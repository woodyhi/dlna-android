package cn.cj.dlna.dms;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;

/**
 * Created by June on 2017/3/7.
 */

public class ContentDirectoryCommand {
	AndroidUpnpService upnpService;

	public ContentDirectoryCommand(AndroidUpnpService upnpService){
		this.upnpService = upnpService;
	}

	public void browse(Device device, String directoryId) {
		Service service = device.findService(new UDAServiceType("ContentDirectory"));
		ActionCallback actionCallback = new Browse(service, directoryId, BrowseFlag.DIRECT_CHILDREN, "*", 0,
				null, new SortCriterion(true, "dc:title")) {
			@Override
			public void received(ActionInvocation actionInvocation, DIDLContent didlContent) {
				System.out.println("=======contentDirectory=====success===" + didlContent.getContainers().get(0).getTitle());
			}

			@Override
			public void updateStatus(Status status) {

			}

			@Override
			public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
				System.out.println("======contentDirectory======fail===" + s);
			}
		};
		upnpService.getControlPoint().execute(actionCallback);
	}


	public interface Callback{
		void onSuccess();
		void onFail();
	}
}
