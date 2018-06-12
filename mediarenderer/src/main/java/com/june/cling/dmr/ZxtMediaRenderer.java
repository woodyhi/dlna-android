
package com.june.cling.dmr;

import android.content.Context;
import android.util.Log;


import com.june.cling.utils.NetworkUtil;

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

public class ZxtMediaRenderer {

    public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 500;

    private static final String TAG = "GstMediaRenderer";

    final protected LocalServiceBinder binder = new AnnotationLocalServiceBinder();

    // These are shared between all "logical" player instances of a single service
    final protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    final protected LastChange renderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    final protected Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> mediaPlayers;

    final protected ServiceManager<ZxtConnectionManagerService> connectionManager;
    final protected LastChangeAwareServiceManager<AVTransportService> avTransport;
    final protected LastChangeAwareServiceManager<AudioRenderingControl> renderingControl;

    final protected LocalDevice device;

   protected Context mContext;

    public ZxtMediaRenderer(int numberOfPlayers,Context context) {
         mContext = context;

        // This is the backend which manages the actual player instances
        mediaPlayers = new ZxtMediaPlayers(
                numberOfPlayers,
                context,
                avTransportLastChange,
                renderingControlLastChange
        ) {
            // These overrides connect the player instances to the output/display
            @Override
            protected void onPlay(ZxtMediaPlayer player) {
//                getDisplayHandler().onPlay(player);
            }

            @Override
            protected void onStop(ZxtMediaPlayer player) {
//                getDisplayHandler().onStop(player);
            }
        };

        // The connection manager doesn't have to do much, HTTP is stateless
        LocalService connectionManagerService = binder.read(ZxtConnectionManagerService.class);
        connectionManager =
                new DefaultServiceManager(connectionManagerService) {
                    @Override
                    protected Object createServiceInstance() throws Exception {
                        return new ZxtConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionManager);

        // The AVTransport just passes the calls on to the backend players
        LocalService<AVTransportService> avTransportService = binder.read(AVTransportService.class);
        avTransport =
                new LastChangeAwareServiceManager<AVTransportService>(
                        avTransportService,
                        new AVTransportLastChangeParser()
                ) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(avTransportLastChange, mediaPlayers);
                    }
                };
        avTransportService.setManager(avTransport);

        // The Rendering Control just passes the calls on to the backend players
        LocalService<AudioRenderingControl> renderingControlService = binder.read(AudioRenderingControl.class);
        renderingControl =
                new LastChangeAwareServiceManager<AudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected AudioRenderingControl createServiceInstance() throws Exception {
                        return new AudioRenderingControl(renderingControlLastChange, mediaPlayers);
                    }
                };
        renderingControlService.setManager(renderingControl);

        try {
            UDN  udn = createUDN("msidmr");

            device = new LocalDevice(
                    new DeviceIdentity(udn),
                    new UDADeviceType("MediaRenderer", 1),
                    new DeviceDetails(
                             "June Local Renderer" + " (" + android.os.Build.MODEL + ")",
                            new ManufacturerDetails(android.os.Build.MANUFACTURER),
                            new ModelDetails(
                                    "june dlna",
                                    "june media renderer",
                                    "1",
                                    "http://4thline.org/projects/cling/mediarenderer/"
                            ),
                            new DLNADoc[] {
                                new DLNADoc("DMR", DLNADoc.Version.V1_5)
                            },
                            new DLNACaps(new String[] {
                                 "av-upload", "image-upload", "audio-upload"
                            })
                    ),
                    new Icon[]{createDefaultDeviceIcon()},
                    new LocalService[]{
                            avTransportService,
                            renderingControlService,
                            connectionManagerService
                    }
            );
            Log.i(TAG,  "getType: " +  device.getType().toString());
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }

        runLastChangePushThread();
    }

    // The backend player instances will fill the LastChange whenever something happens with
    // whatever event messages are appropriate. This loop will periodically flush these changes
    // to subscribers of the LastChange state variable of each service.
    protected void runLastChangePushThread() {
        // TODO: We should only run this if we actually have event subscribers
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        avTransport.fireLastChange();
                        renderingControl.fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "runLastChangePushThread ex", ex);
                }
            }
        }.start();
    }

    public LocalDevice getDevice() {
        return device;
    }


    synchronized public Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> getMediaPlayers() {
        return mediaPlayers;
    }

    synchronized public void stopAllMediaPlayers() {
        for (ZxtMediaPlayer mediaPlayer : mediaPlayers.values()) {
            TransportState state =
                mediaPlayer.getCurrentTransportInfo().getCurrentTransportState();
            if (!state.equals(TransportState.NO_MEDIA_PRESENT) ||
                    state.equals(TransportState.STOPPED)) {
                Log.i(TAG, "Stopping player instance: " + mediaPlayer.getInstanceId());
//                mediaPlayer.stop();
            }
        }
    }

    public ServiceManager<ZxtConnectionManagerService> getConnectionManager() {
        return connectionManager;
    }

    public ServiceManager<AVTransportService> getAvTransport() {
        return avTransport;
    }

    public ServiceManager<AudioRenderingControl> getRenderingControl() {
        return renderingControl;
    }

    protected Icon createDefaultDeviceIcon() {
        try {
            return new Icon("image/png", 48, 48, 32, "msi.png", mContext.getResources().getAssets()
                    .open("ic_launcher.png"));
        } catch (IOException e) {
            Log.w(TAG, "createDefaultDeviceIcon IOException");
            return null;
        }
    }


    /**
     * ref to {@link UDN#uniqueSystemIdentifier}
     * @param salt
     * @return
     */
    private UDN createUDN(String salt){
        StringBuilder systemSalt = new StringBuilder();
        systemSalt.append(android.os.Build.MODEL);
        systemSalt.append(android.os.Build.MANUFACTURER);
        systemSalt.append(NetworkUtil.getWifiMac(mContext));

        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(systemSalt.toString().getBytes("UTF-8"));
            return new UDN(new UUID(new BigInteger(-1, hash).longValue(), salt.hashCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        new UUID(0, 10).toString();
        return UDN.valueOf(systemSalt.toString());
    }
}
