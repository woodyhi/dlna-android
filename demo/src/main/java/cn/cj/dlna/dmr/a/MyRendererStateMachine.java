package cn.cj.dlna.dmr.a;

import org.fourthline.cling.support.avtransport.impl.AVTransportStateMachine;
import org.seamless.statemachine.States;

/**
 * Created by June on 2018/6/11.
 */

@States({
        MyRendererNoMediaPresent.class,
        MyRendererStopped.class,
        MyRendererPlaying.class
})
public interface MyRendererStateMachine extends AVTransportStateMachine{
}
