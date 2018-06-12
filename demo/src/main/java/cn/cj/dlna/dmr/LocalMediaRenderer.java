package cn.cj.dlna.dmr;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.lastchange.LastChangeParser;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import cn.cj.dlna.R;
import cn.cj.dlna.dmr.a.MyRendererNoMediaPresent;
import cn.cj.dlna.dmr.a.MyRendererStateMachine;
import cn.cj.dlna.util.NetworkUtil;

/**
 * Created by June on 2018/6/11.
 */
public class LocalMediaRenderer {
    private String TAG = LocalMediaRenderer.class.getSimpleName();
    private Context context;

    private UDN udn = null;
    private LocalDevice localDevice;
    private LocalService localService;


    private static volatile LocalMediaRenderer sInstance;

    public static LocalMediaRenderer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LocalMediaRenderer(context);
        }
        return sInstance;
    }

    private LocalMediaRenderer(Context context) {
        this.context = context;
        try {
            createLocalDevice();
        } catch (ValidationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalService<AVTransportService> createAVTransportService() throws Exception {
        LocalService<AVTransportService> service =
                new AnnotationLocalServiceBinder().read(AVTransportService.class);

        // Service's which have "logical" instances are very special, they use the
        // "LastChange" mechanism for eventing. This requires some extra wrappers.
        LastChangeParser lastChangeParser = new AVTransportLastChangeParser();

        service.setManager(
                new LastChangeAwareServiceManager<AVTransportService>(service, lastChangeParser) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(
                                MyRendererStateMachine.class,   // All states
                                MyRendererNoMediaPresent.class  // Initial state
                        );
                    }
                }
        );
        return service;
    }

    public static LocalService<AudioRenderingControlService> createRenderingControlService() throws Exception {

        LocalService<AudioRenderingControlService> service =
                new AnnotationLocalServiceBinder().read(AudioRenderingControlService.class);

        LastChangeParser lastChangeParser = new RenderingControlLastChangeParser();

        service.setManager(
                new LastChangeAwareServiceManager<>(
                        service,
                        AudioRenderingControlService.class,
                        lastChangeParser
                )
        );
        return service;
    }


    public void createLocalDevice() throws Exception {
        localService = createAVTransportService();

        String version = "";
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Application version name not found");
        }

        DeviceDetails details = new DeviceDetails(
                "JUNE Media Renderer (" + android.os.Build.MODEL + ")",
                new ManufacturerDetails(android.os.Build.MANUFACTURER),
                new ModelDetails(context.getString(R.string.app_name)),
                context.getString(R.string.app_name),
                version);

        //  udn = UDN.valueOf(new UUID(0, 10).toString());
        udn = createUDN("mdmr");
        Log.d(TAG, "udn --- " + udn);
        DeviceType type = new UDADeviceType("MediaRenderer", 1);

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, localService);
    }

    public LocalDevice createDevice() throws Exception {
        return new LocalDevice(
                new DeviceIdentity(createUDN("mdmr")),
                new UDADeviceType("MediaRenderer"),
                new DeviceDetails("JUNE Media Renderer (" + android.os.Build.MODEL + ")"),
                new LocalService[]{
                        createAVTransportService(),
                        createRenderingControlService()
                }
        );
    }

    /**
     * ref to {@link UDN#uniqueSystemIdentifier}
     *
     * @param salt
     * @return
     */
    private UDN createUDN(String salt) {
        StringBuilder systemSalt = new StringBuilder();
        systemSalt.append(android.os.Build.MODEL);
        systemSalt.append(android.os.Build.MANUFACTURER);
        systemSalt.append(NetworkUtil.getWifiMac(context));

        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(systemSalt.toString().getBytes("UTF-8"));
            return new UDN(new UUID(new BigInteger(-1, hash).longValue(), salt.hashCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        new UUID(0, 10).toString();
        return UDN.valueOf(systemSalt.toString());
    }


    public LocalDevice getLocalDevice() {
        return localDevice;
    }

    public static class AudioRenderingControlService extends AbstractAudioRenderingControl {

        @Override
        public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
            return false;
        }

        @Override
        public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute) throws RenderingControlException {

        }

        @Override
        public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
            return new UnsignedIntegerTwoBytes(50);
        }

        @Override
        public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {

        }

        @Override
        protected Channel[] getCurrentChannels() {
            return new Channel[]{
                    Channel.Master
            };
        }

        @Override
        public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
            return new UnsignedIntegerFourBytes[0];
        }
    }
}
