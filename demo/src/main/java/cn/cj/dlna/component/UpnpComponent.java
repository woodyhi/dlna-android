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
import java.util.Vector;

import cn.cj.dlna.R;
import cn.cj.dlna.dmc.LocalMediaServer;

/**
 * Created by June on 2017/3/7.
 */

public class UpnpComponent {
	private Context context;

	private AndroidUpnpService upnpService;
	private BrowseRegistryListener registryListener = new BrowseRegistryListener();

	private static volatile UpnpComponent sInstance;

	private List<LocalDevice> localDeviceList = new ArrayList<>();

	private Vector<DefaultRegistryListener> registries = new Vector<>();

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
				registryListener.deviceAdded(null, device);
			}

			// Search asynchronously for all devices, they will respond soon
			upnpService.getControlPoint().search();
		}

		public void onServiceDisconnected(ComponentName className) {
			upnpService = null;
		}
	};


	private UpnpComponent() {
	}

	public static UpnpComponent getsInstance() {
		if (sInstance == null) {
			sInstance = new UpnpComponent();
		}
		return sInstance;
	}

	public void init(Context context){
		this.context = context.getApplicationContext();
	}

	public void start() {
		context.getApplicationContext().bindService(new Intent(context, AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	public void stop() {
		context.getApplicationContext().unbindService(serviceConnection);
		context.stopService(new Intent(context, AndroidUpnpServiceImpl.class));
	}

	public AndroidUpnpService getAndroidUpnpService(){
		return upnpService;
	}

	public void addLocalDevice(LocalDevice localDevice){
		localDeviceList.add(localDevice);
	}

	public void addRegistryListener(DefaultRegistryListener listener){
		registries.add(listener);
	}

	public void removeRegistryListener(DefaultRegistryListener listener){
		registries.remove(listener);
	}

	protected class BrowseRegistryListener extends DefaultRegistryListener {

		/* Discovery performance optimization for very slow Android devices! */
		@Override
		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
				super.remoteDeviceDiscoveryStarted(registry, device);
				System.out.println("************* remoteDeviceDiscoveryStarted");
				if(!registries.isEmpty()){
					for(RegistryListener listener : registries){
						listener.remoteDeviceDiscoveryStarted(registry, device);
					}
				}
		}

		@Override
		public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
			super.remoteDeviceDiscoveryFailed(registry, device, ex);
			if(!registries.isEmpty()){
				for(RegistryListener listener : registries){
					listener.remoteDeviceDiscoveryFailed(registry, device, ex);
				}
			}
			Toast.makeText(
					context,
					"Discovery failed of '" + device.getDisplayString() + "': "
							+ (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
					Toast.LENGTH_LONG
			).show();
		}
		/* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

		@Override
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			super.remoteDeviceAdded(registry, device);
			System.out.println("************* remoteDeviceAdded");
			if(!registries.isEmpty()){
				for(RegistryListener listener : registries){
					listener.remoteDeviceAdded(registry, device);
				}
			}
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			super.remoteDeviceRemoved(registry, device);
			if(!registries.isEmpty()){
				for(RegistryListener listener : registries){
					listener.remoteDeviceRemoved(registry, device);
				}
			}
		}

		@Override
		public void localDeviceAdded(Registry registry, LocalDevice device) {
			super.localDeviceAdded(registry, device);
			System.out.println("************* localDeviceAdded");
			if(!registries.isEmpty()){
				for(RegistryListener listener : registries){
					listener.localDeviceAdded(registry, device);
				}
			}
		}

		@Override
		public void localDeviceRemoved(Registry registry, LocalDevice device) {
			super.localDeviceRemoved(registry, device);
			if(!registries.isEmpty()){
				for(RegistryListener listener : registries){
					listener.localDeviceRemoved(registry, device);
				}
			}
		}

		@Override
		public void deviceAdded(Registry registry, final Device device) {
			Service localService = device.findService(new UDAServiceType("AVTransport"));
			if (localService != null) {
			}

			if(!registries.isEmpty()){
				for(DefaultRegistryListener listener : registries){
					listener.deviceRemoved(registry, device);
				}
			}
		}

		@Override
		public void deviceRemoved(Registry registry, final Device device) {
			if(!registries.isEmpty()){
				for(DefaultRegistryListener listener : registries){
					listener.deviceRemoved(registry, device);
				}
			}
		}
	}

}
