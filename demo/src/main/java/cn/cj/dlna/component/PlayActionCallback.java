package cn.cj.dlna.component;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

/**
 * Created by june on 2017/3/4.
 */

public class PlayActionCallback extends ActionCallback {
    protected PlayActionCallback(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    protected PlayActionCallback(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    @Override
    public void success(ActionInvocation actionInvocation) {
        System.out.println("111111111111    PlayActionCallback success");
    }

    @Override
    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
        System.out.println("11111111111  PlayActionCallback failure");
    }
}
