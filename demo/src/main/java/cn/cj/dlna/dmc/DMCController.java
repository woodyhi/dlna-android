package cn.cj.dlna.dmc;


import org.eclipse.jetty.util.UrlEncoded;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.item.VideoItem;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import cn.cj.dlna.MainActivity;
import cn.cj.dlna.a.TrackMetadata;

/**
 * Created by june on 2017/3/4.
 */

public class DMCController {

    public void contentDirectory(final AndroidUpnpService upnpService, final Device device) {
        Service service = device.findService(new UDAServiceType("ContentDirectory"));
        ActionCallback actionCallback = new Browse(service, "0", BrowseFlag.DIRECT_CHILDREN, "*", 0,
                null, new SortCriterion(true, "dc:title")) {
            @Override
            public void received(ActionInvocation actionInvocation, DIDLContent didlContent) {
                try {
                    System.out.println("=======contentDirectory=====success===" + didlContent.getContainers().get(0).getTitle());
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            String url = MainActivity.PLAYURL;
            final TrackMetadata trackMetadata = new TrackMetadata("", title, "",
                    "", "", url, "object.item.videoItem");

            ActionCallback actionCallback = new SetAVTransportURI(service, uri, trackMetadata.getXML()) {
                @Override
                public void success(@SuppressWarnings("rawtypes") ActionInvocation invocation) {
                    super.success(invocation);
                    System.out.println("设置URL成功");
                    play(upnpService, service);
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


    public void play(AndroidUpnpService upnpService, Service service) {
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

    public void executeAction(UpnpService upnpService, Service service) {

        ActionInvocation setTargetInvocation = new SetTargetActionInvocation(service);

        // Executes asynchronous in the background
        upnpService.getControlPoint().execute(
                new ActionCallback(setTargetInvocation) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        assert invocation.getOutput().length == 0;
                        System.out.println("Successfully called action!");
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        System.err.println(defaultMsg);
                    }
                }
        );

    }

    class SetTargetActionInvocation extends ActionInvocation {

        SetTargetActionInvocation(Service service) {
            super(service.getAction("SetTarget"));
            try {

                // Throws InvalidValueException if the value is of wrong type
                setInput("NewTargetValue", true);

            } catch (InvalidValueException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }
    }
}
