package se.kth.gossipbeb.event;

import se.sics.kompics.KompicsEvent;
import se.sics.ktoolbox.util.network.KAddress;

import java.io.Serializable;

/**
 * Created by 62maxime on 03/04/2017.
 */
public class GBEBDeliver implements KompicsEvent, Serializable {

    private KAddress source;
    private KompicsEvent payload;

    public GBEBDeliver(KAddress source, KompicsEvent payload) {
        this.source = source;
        this.payload = payload;
    }

    public KompicsEvent getPayload() {
        return payload;
    }
}
