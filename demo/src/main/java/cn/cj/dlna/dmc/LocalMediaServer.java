package cn.cj.dlna.dmc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import cn.cj.dlna.R;
import cn.cj.dlna.util.NetworkUtil;
import fi.iki.elonen.SimpleWebServer;

/**
 * Created by June on 2017/3/7.
 */

public class LocalMediaServer extends SimpleWebServer{
	private static final String TAG = "LocalMediaServer";

	private UDN udn = null;
	private LocalDevice  localDevice;
	private LocalService localService;

	private static int port = 8192;
	private InetAddress localAddress;


	private Context context;

	private static LocalMediaServer sInstance;
	public static LocalMediaServer getInstance(Context context){
		if(sInstance == null){
			sInstance = new LocalMediaServer(context);
		}
		return sInstance;
	}

	public LocalMediaServer(Context context){
		super(null, port, null, true);
		this.context = context;
		try {
			createLocalDevice();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		}

		try {
			localAddress = NetworkUtil.getLocalIpAddress(context);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		ContentDirectoryService contentDirectoryService = (ContentDirectoryService)localService.getManager().getImplementation();
		contentDirectoryService.setContext(context);
		contentDirectoryService.setBaseURL(getAddress());
	}




	public void createLocalDevice() throws UnknownHostException, ValidationException {
		Log.i(TAG, "Creating media server !");
		localAddress = NetworkUtil.getLocalIpAddress(context);


		localService = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
		localService.setManager(new DefaultServiceManager<ContentDirectoryService>(localService, ContentDirectoryService.class));

		String version = "";
		try {
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Application version name not found");
		}

		DeviceDetails details = new DeviceDetails(android.os.Build.MODEL,
				new ManufacturerDetails(android.os.Build.MANUFACTURER),
				new ModelDetails(context.getString(R.string.app_name)),
				context.getString(R.string.app_name), version);

		udn = UDN.valueOf(new UUID(0, 10).toString());
		DeviceType type = new UDADeviceType("MediaServer", 1);

		localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, localService);
	}

	public String getAddress() {
		return localAddress.getHostAddress() + ":" + port;
	}

	public LocalDevice getLocalDevice(){
		return localDevice;
	}
}
