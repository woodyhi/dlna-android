package cn.cj.dlna.component;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.util.ArrayList;
import java.util.List;

import cn.cj.dlna.dmc.LocalMediaServer;

/**
 * Created by June on 2017/3/7.
 */

public class UpnpComponent {
	private Context context;

	private AndroidUpnpService upnpService;
	private BrowseRegistryListener registryListener = new BrowseRegistryListener();

	private static UpnpComponent sInstance;

	private List<LocalDevice> localDeviceList = new ArrayList<>();

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			System.out.println("----------onServiceConnected");
			upnpService = (AndroidUpnpService) service;

			// Get ready for future device advertisements
			upnpService.getRegistry().addListener(registryListener);

			try {
				LocalMediaServer localMediaServer = new LocalMediaServer(context);
				localMediaServer.start();
			}catch (Exception e){
				e.printStackTrace();
			}

			// Now add all devices to the list we already know about
			for (Device device : upnpService.getRegistry().getDevices()) {
				registryListener.deviceAdded(device);
			}

			// Search asynchronously for all devices, they will respond soon
			upnpService.getControlPoint().search();
		}

		public void onServiceDisconnected(ComponentName className) {
			upnpService = null;
		}
	};


	private UpnpComponent(Context context) {
		this.context = context;
		start();
	}

	public static UpnpComponent getsInstance(Context context) {
		if (sInstance == null) {
			sInstance = new UpnpComponent(context);
		}
		return sInstance;
	}

	public void start() {
		context.getApplicationContext().bindService(new Intent(context.getApplicationContext(), AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	public void close() {
		context.getApplicationContext().unbindService(serviceConnection);
	}

	public AndroidUpnpService getAndroidUpnpService(){
		return upnpService;
	}

	public void addLocalDevice(LocalDevice localDevice){
		localDeviceList.add(localDevice);
	}

	public void addRegistryListener(RegistryListener listener){
		upnpService.getRegistry().addListener(listener);
	}

	public void removeRegistryListener(RegistryListener listener){
		upnpService.getRegistry().removeListener(listener);
	}

	protected class BrowseRegistryListener extends DefaultRegistryListener {

		/* Discovery performance optimization for very slow Android devices! */
		@Override
		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
			System.out.println("************* remoteDeviceDiscoveryStarted");
			//            deviceAdded(device);
		}

		@Override
		public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
			Toast.makeText(
					context,
					"Discovery failed of '" + device.getDisplayString() + "': "
							+ (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
					Toast.LENGTH_LONG
			).show();
			deviceRemoved(device);
		}
		/* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

		@Override
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			System.out.println("************* remoteDeviceAdded");
			deviceAdded(device);
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			deviceRemoved(device);
		}

		@Override
		public void localDeviceAdded(Registry registry, LocalDevice device) {
			System.out.println("************* localDeviceAdded");
			deviceAdded(device);
		}

		@Override
		public void localDeviceRemoved(Registry registry, LocalDevice device) {
			deviceRemoved(device);
		}

		public void deviceAdded(final Device device) {
			Service localService = device.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
				//						((DeviceAdapter) listView.getAdapter()).add(device);
			}
			Log.d("----------deviceAdded", device.getDisplayString()
					+ "\n" + device.getDetails().getFriendlyName() // 用这个名字显示设备
					+ "\n" + device.getDetails().getSerialNumber()
					+ "\n" + device.getDetails().getUpc()
					+ "\n" + device.getDetails().getBaseURL()
					+ "\n" + device.getDetails().getPresentationURI()
					+ "\n" + device.getDetails().getModelDetails().getModelName());


		}

		public void deviceRemoved(final Device device) {
			//					((DeviceAdapter) listView.getAdapter()).remove(device);
		}
	}

}
