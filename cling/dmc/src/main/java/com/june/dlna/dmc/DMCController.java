package com.june.dlna.dmc;


import com.june.dlna.TrackMetadata;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;

/**
 * @author june
 * @date 2017/3/4.
 */
public class DMCController {

    /** 视频播放地址 */
    public void setAVTransport(final AndroidUpnpService upnpService, final Device device, String uri) {
        try {
            final Service service = device.findService(new UDAServiceType("AVTransport"));
            if (service == null) {
                System.out.println("AVTransport service is null");
                return;
            }

//            VideoItem upnpItem = new VideoItem();
//            upnpItem.setTitle("直播");
//            List<Res> ress = new ArrayList<>();
//            Res res = new Res();
//            res.setValue(MainActivity.PLAYURL);
//            ress.add(res);
//            upnpItem.setResources(ress);
//            String type = "videoItem";
//
//            // TODO genre && artURI
//            final TrackMetadata trackMetadata = new TrackMetadata(upnpItem.getId(), upnpItem.getTitle(),
//                    upnpItem.getCreator(), "", "", upnpItem.getResources().get(0).getValue(),
//                    "object.item." + type);
            String title = "直播"; // 中文传输后显示乱码
            String url = uri;
            final TrackMetadata trackMetadata = new TrackMetadata("", title, "",
                    "", "", url, "object.item.videoItem");

            ActionCallback actionCallback = new SetAVTransportURI(service, uri, trackMetadata.getXML()) {
                @Override
                public void success(@SuppressWarnings("rawtypes") ActionInvocation invocation) {
                    super.success(invocation);
                    System.out.println("设置URL成功");
                    play(upnpService, device);
                }

                public void failure(@SuppressWarnings("rawtypes") ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    System.out.println("设置URI失败：\n" + operation.getResponseDetails() + "\n" + defaultMsg);
                }
            };
            upnpService.getControlPoint().execute(actionCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void play(AndroidUpnpService upnpService, Device device) {
        final Service service = device.findService(new UDAServiceType("AVTransport"));
        ActionCallback playa = new Play(service) {
            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                System.out.println("播放成功");
            }

            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                System.out.println("播放失败");

            }
        };
        upnpService.getControlPoint().execute(playa);
    }

    public void pause(AndroidUpnpService upnpService, Device device) {
        final Service service = device.findService(new UDAServiceType("AVTransport"));
        ActionCallback pauseA = new Pause(service) {
            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                System.out.println("暂停失败 " + s);
            }
        };
        upnpService.getControlPoint().execute(pauseA);
    }

    public void stop(AndroidUpnpService upnpService, Device device) {
        final Service service = device.findService(new UDAServiceType("AVTransport"));
        Stop stop = new Stop(service) {
            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                System.out.println("stop fail");
            }
        };
        upnpService.getControlPoint().execute(stop);
    }

}
