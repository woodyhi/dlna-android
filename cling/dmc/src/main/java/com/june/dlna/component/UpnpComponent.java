package com.june.dlna.component;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DLNA UPNP 管理组件
 *
 * @author june
 * @date 2017/3/7.
 */
public class UpnpComponent {
	private Logger logger = Logger.getLogger(UpnpComponent.class.getSimpleName());

	private Context context;

	private AndroidUpnpService upnpService;
	private InternalRegistryListener internalRegistryListener = new InternalRegistryListener();

	private static volatile UpnpComponent sInstance;

	private List<LocalDevice> localDeviceList = new ArrayList<>();

	private Vector<DefaultRegistryListener> registries = new Vector<>();

	private ServiceConnectCallback connectCallback;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			upnpService = (AndroidUpnpService) service;

			// Get ready for future device advertisements
			upnpService.getRegistry().addListener(internalRegistryListener);

			// Now add all devices to the list we already know about
			for (Device device : upnpService.getRegistry().getDevices()) {
				internalRegistryListener.deviceAdded(null, device);
			}

			// Search asynchronously for all devices, they will respond soon
			upnpService.getControlPoint().search();

			if(connectCallback != null){
				connectCallback.onConnected(upnpService);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			upnpService = null;
			connectCallback.onDisconnected();
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

	public void setConnectionCallback(ServiceConnectCallback connectionCallback) {
		this.connectCallback = connectionCallback;
	}

	protected class InternalRegistryListener extends DefaultRegistryListener {

		/* Discovery performance optimization for very slow Android devices! */
		@Override
		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
				super.remoteDeviceDiscoveryStarted(registry, device);
				logger.log(Level.INFO, "****** remoteDeviceDiscoveryStarted " + device.getDetails().getFriendlyName());
				if(!registries.isEmpty()){
					for(RegistryListener listener : registries){
						listener.remoteDeviceDiscoveryStarted(registry, device);
					}
				}
		}

		@Override
		public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
			super.remoteDeviceDiscoveryFailed(registry, device, ex);
			logger.log(Level.INFO, "****** remoteDeviceDiscoveryFailed " + device.getDetails().getFriendlyName());
			if(!registries.isEmpty()){
				for(RegistryListener listener : registries){
					listener.remoteDeviceDiscoveryFailed(registry, device, ex);
				}
			}
//			Toast.makeText(
//					context,
//					"Discovery failed of '" + device.getDisplayString() + "': "
//							+ (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
//					Toast.LENGTH_LONG
//			).show();
		}
		/* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

		@Override
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			logger.log(Level.INFO, "****** remoteDeviceAdded " + device.getDetails().getFriendlyName());
			super.remoteDeviceAdded(registry, device);
//			if(!registries.isEmpty()){
//				for(RegistryListener listener : registries){
//					listener.remoteDeviceAdded(registry, device);
//				}
//			}
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			logger.log(Level.INFO, "****** remoteDeviceRemoved " + device.getDetails().getFriendlyName());
			super.remoteDeviceRemoved(registry, device);
//			if(!registries.isEmpty()){
//				for(RegistryListener listener : registries){
//					listener.remoteDeviceRemoved(registry, device);
//				}
//			}
		}

		@Override
		public void localDeviceAdded(Registry registry, LocalDevice device) {
			logger.log(Level.INFO, "****** localDeviceAdded " + device.getDetails().getFriendlyName());
			super.localDeviceAdded(registry, device);
//			if(!registries.isEmpty()){
//				for(RegistryListener listener : registries){
//					listener.localDeviceAdded(registry, device);
//				}
//			}
		}

		@Override
		public void localDeviceRemoved(Registry registry, LocalDevice device) {
			logger.log(Level.INFO, "****** localDeviceRemoved " + device.getDetails().getFriendlyName());
			super.localDeviceRemoved(registry, device);
//			if(!registries.isEmpty()){
//				for(RegistryListener listener : registries){
//					listener.localDeviceRemoved(registry, device);
//				}
//			}
		}

		@Override
		public void deviceAdded(Registry registry, final Device device) {
			Log.d("---", "---deviceAdded " + device.getDetails().getFriendlyName());
			Log.d("------deviceAdded", "\n" + device.getDisplayString()
					+ "\n" + device.getDetails().getFriendlyName() // 用这个名字显示设备
					+ "\n" + device.getDetails().getSerialNumber()
					+ "\n" + device.getDetails().getBaseURL()
					+ "\n" + device.getDetails().getModelDetails().getModelName()
					+ "\n" + device.getType()
					+ "\n" + device.getType().getType()
					+ "\n" + device.getDetails().getManufacturerDetails().getManufacturerURI()
					+ "\n" + device.getDetails().getModelDetails().getModelURI()
					+ "\n" + device.getDetails().getPresentationURI()
			);

			Service[] services = device.findServices();
			String s = "";
			for(Service service: services){
				s += " " + service.getServiceType().getType();
			}
			Log.e("---found Services", s);

			final Service service = device.findService(new UDAServiceType("AVTransport"));
			if(service instanceof RemoteService){
				RemoteService remoteService = (RemoteService)service;

				// Figure out the remote URL where we'd like to send the action request to
				URL controLURL = remoteService.getDevice().normalizeURI(remoteService.getControlURI());
				Log.e("---AVTransport", controLURL.toString());
			}


			if(!registries.isEmpty()){
				for(DefaultRegistryListener listener : registries){
					listener.deviceAdded(registry, device);
				}
			}
		}

		@Override
		public void deviceRemoved(Registry registry, final Device device) {
			Log.d("---", "deviceRemoved " + device.getDetails().getFriendlyName());
			if(!registries.isEmpty()){
				for(DefaultRegistryListener listener : registries){
					listener.deviceRemoved(registry, device);
				}
			}
		}
	}

	public interface ServiceConnectCallback {
		void onConnected(AndroidUpnpService upnpService);
		void onDisconnected();
	}

}
