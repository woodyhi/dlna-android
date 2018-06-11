package cn.cj.dlna.dmr.a;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Stopped;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

/**
 * Created by June on 2018/6/11.
 */
public class MyRendererStopped extends Stopped {
    public MyRendererStopped(AVTransport transport) {
        super(transport);
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String s) {
        return MyRendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        return MyRendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> play(String s) {
        return MyRendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        return MyRendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        return MyRendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode seekMode, String s) {
        return MyRendererStopped.class;
    }
}
