package cn.cj.dlna.dmr.a;

import android.util.Log;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;

import java.net.URI;

import cn.cj.dlna.a.TrackMetadata;

/**
 * Created by June on 2018/6/11.
 */
public class MyRendererNoMediaPresent extends NoMediaPresent {
    private final String TAG = MyRendererNoMediaPresent.class.getSimpleName();

    public MyRendererNoMediaPresent(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
//        Log.d(TAG, "1---onEntry-- uri : " + getTransport().getMediaInfo().getCurrentURI());
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        Log.d(TAG, "2---setTransportURI-- uri : " + uri.toString()
                + "\n title : " + new TrackMetadata(metaData).title
                + "\n metaData : " + metaData);
        getTransport().setMediaInfo(
                new MediaInfo(uri.toString(), metaData)
        );

        // If you can, you should find and set the duration of the track here!
        getTransport().setPositionInfo(
                new PositionInfo(1, metaData, uri.toString())
        );

        // It's up to you what "last changes" you want to announce to event listeners
        getTransport().getLastChange().setEventedValue(
                getTransport().getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );

        return MyRendererStopped.class;
    }
}
