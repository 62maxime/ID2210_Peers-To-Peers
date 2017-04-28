/*
 * 2016 Royal Institute of Technology (KTH)
 *
 * LSelector is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.tests.beb_test.sim.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.ktoolbox.cc.heartbeat.CCHeartbeatPort;
import se.sics.ktoolbox.croupier.CroupierPort;
import se.sics.ktoolbox.omngr.bootstrap.BootstrapClientComp;
import se.sics.ktoolbox.overlaymngr.OverlayMngrComp;
import se.sics.ktoolbox.overlaymngr.OverlayMngrPort;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.nat.NatAwareAddress;
import se.sics.ktoolbox.util.overlays.view.OverlayViewUpdatePort;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class BEBTestHostMngrComp extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(BEBTestHostMngrComp.class);
    private String logPrefix = " ";

    //*****************************CONNECTIONS**********************************
    Positive<Timer> timerPort = requires(Timer.class);
    Positive<Network> networkPort = requires(Network.class);
    //***************************EXTERNAL_STATE*********************************
    private KAddress selfAdr;
    private KAddress bootstrapServer;
    private OverlayId croupierId;
    //***************************INTERNAL_STATE*********************************
    private Component bootstrapClientComp;
    private Component overlayMngrComp;
    private Component appMngrComp;
    //***************************SIMULATION_STATE*********************************
    private int broadcastCount;

    public BEBTestHostMngrComp(Init init) {
        selfAdr = init.selfAdr;
        broadcastCount = init.broadcastCount;
        logPrefix = "<nid:" + selfAdr.getId() + ">";
        LOG.info("{}initiating...", logPrefix);

        bootstrapServer = init.bootstrapServer;
        croupierId = init.croupierId;

        connectBootstrapClient();
        connectOverlayMngr();
        connectApp();


        subscribe(handleStart, control);
    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);

        }
    };

    private void connectBootstrapClient() {
        bootstrapClientComp = create(BootstrapClientComp.class, new BootstrapClientComp.Init(selfAdr, bootstrapServer));
        connect(bootstrapClientComp.getNegative(Timer.class), timerPort, Channel.TWO_WAY);
        connect(bootstrapClientComp.getNegative(Network.class), networkPort, Channel.TWO_WAY);
    }

    private void connectOverlayMngr() {
        OverlayMngrComp.ExtPort extPorts = new OverlayMngrComp.ExtPort(timerPort, networkPort,
                bootstrapClientComp.getPositive(CCHeartbeatPort.class));
        overlayMngrComp = create(OverlayMngrComp.class, new OverlayMngrComp.Init((NatAwareAddress) selfAdr, extPorts));
    }

    private void connectApp() {
        BEBTestAppMngrComp.ExtPort extPorts = new BEBTestAppMngrComp.ExtPort(timerPort, networkPort, overlayMngrComp.getPositive(CroupierPort.class), overlayMngrComp.getNegative(OverlayViewUpdatePort.class));
        appMngrComp = create(BEBTestAppMngrComp.class, new BEBTestAppMngrComp.Init(extPorts, selfAdr, croupierId, broadcastCount));
        connect(appMngrComp.getNegative(OverlayMngrPort.class), overlayMngrComp.getPositive(OverlayMngrPort.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<BEBTestHostMngrComp> {

        public final KAddress selfAdr;
        public final KAddress bootstrapServer;
        public final OverlayId croupierId;
        public final int broadcastCount;

        public Init(KAddress selfAdr, KAddress bootstrapServer, OverlayId croupierId, int broadcastCount) {
            this.selfAdr = selfAdr;
            this.bootstrapServer = bootstrapServer;
            this.croupierId = croupierId;
            this.broadcastCount = broadcastCount;
        }
    }
}
