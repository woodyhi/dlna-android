package cn.cj.dlna.dmr.a;

import android.util.Log;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

/**
 * Created by June on 2018/6/11.
 */
public class MyRendererPlaying extends Playing {
    private final String TAG = MyRendererPlaying.class.getSimpleName();

    public MyRendererPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();

        Log.d(TAG, "---onEntry");
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        // Your choice of action here, and what the next state is going to be!
        return MyRendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        // Stop playing!
        return MyRendererStopped.class;
    } // DOC:INC1

    @Override
    public Class<? extends AbstractState> play(String speed) {
        Log.d(TAG, "---play-- speed : " + speed);
        return null;
    }

    @Override
    public Class<? extends AbstractState> pause() {
        return null;
    }

    @Override
    public Class<? extends AbstractState> next() {
        return null;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        return null;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode unit, String target) {
        return null;
    }
}
