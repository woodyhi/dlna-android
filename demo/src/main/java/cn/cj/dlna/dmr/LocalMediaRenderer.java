package cn.cj.dlna.dmr;

import android.content.Context;
import android.content.pm.PackageManager;
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
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.lastchange.LastChangeParser;

import java.util.UUID;

import cn.cj.dlna.R;
import cn.cj.dlna.dmr.a.MyRendererNoMediaPresent;
import cn.cj.dlna.dmr.a.MyRendererStateMachine;

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
        //        MyAVTransportStateMachine m = new MyAVTransportStateMachine();
        //        AVTransportService<AVTransport> ats = new AVTransportService<AVTransport>(MyAVTransportStateMachine.class, MyAVTransportState.class);
        try {
            createLocalDevice();
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    public void createLocalDevice() throws ValidationException {
        localService = new AnnotationLocalServiceBinder().read(AVTransportService.class);

        // Service's which have "logical" instances are very special, they use the
        // "LastChange" mechanism for eventing. This requires some extra wrappers.
        LastChangeParser lastChangeParser = new AVTransportLastChangeParser();
        localService.setManager(
                new LastChangeAwareServiceManager<AVTransportService>(localService, lastChangeParser) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(
                                MyRendererStateMachine.class,   // All states
                                MyRendererNoMediaPresent.class  // Initial state
                        );
                    }
                }
        );

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

        udn = UDN.valueOf(new UUID(0, 10).toString());
        DeviceType type = new UDADeviceType("MediaRenderer", 1);

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, localService);
    }

    public LocalDevice getLocalDevice() {
        return localDevice;
    }
}
