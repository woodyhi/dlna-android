package cn.cj.dlna.dmr;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;

public class AVTransportService extends AbstractAVTransportService {
    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes unsignedIntegerFourBytes, String s, String s1) throws AVTransportException {

    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes unsignedIntegerFourBytes, String s, String s1) throws AVTransportException {

    }

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {
        return null;
    }

    @Override
    public void stop(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void play(UnsignedIntegerFourBytes unsignedIntegerFourBytes, String s) throws AVTransportException {

    }

    @Override
    public void pause(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void record(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void seek(UnsignedIntegerFourBytes unsignedIntegerFourBytes, String s, String s1) throws AVTransportException {

    }

    @Override
    public void next(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void previous(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws AVTransportException {

    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes unsignedIntegerFourBytes, String s) throws AVTransportException {

    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes unsignedIntegerFourBytes, String s) throws AVTransportException {

    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes unsignedIntegerFourBytes) throws Exception {
        return new TransportAction[0];
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return new UnsignedIntegerFourBytes[0];
    }
}
