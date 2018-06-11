package cn.cj.dlna.dmr.a;

import android.util.Log;

import org.fourthline.cling.support.avtransport.impl.AVTransportStateMachine;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

/**
 * Created by June on 2018/6/11.
 */
@Deprecated
public class MyAVTransportStateMachine implements AVTransportStateMachine {
    private final String TAG = MyAVTransportStateMachine.class.getSimpleName();


    @Override
    public void setTransportURI(URI uri, String s) {
        Log.d(TAG, " uri : " + uri.toString() + " \n " + s);
    }

    @Override
    public void setNextTransportURI(URI uri, String s) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void play(String s) {
        Log.d(TAG, " play : \n " + s);

    }

    @Override
    public void pause() {

    }

    @Override
    public void record() {

    }

    @Override
    public void seek(SeekMode seekMode, String s) {

    }

    @Override
    public void next() {

    }

    @Override
    public void previous() {

    }

    @Override
    public AbstractState getCurrentState() {
        return null;
    }

    @Override
    public void forceState(Class<? extends AbstractState> aClass) {

    }
}
