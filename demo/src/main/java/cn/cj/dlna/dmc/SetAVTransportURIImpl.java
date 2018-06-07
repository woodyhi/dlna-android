package cn.cj.dlna.dmc;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;

/**
 * Created by June on 2018/6/7.
 */
public class SetAVTransportURIImpl extends SetAVTransportURI {

    public SetAVTransportURIImpl(Service service, String uri) {
        super(service, uri);
    }

    @Override
    public void success(ActionInvocation invocation) {
        super.success(invocation);
    }

    @Override
    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {

    }
}
