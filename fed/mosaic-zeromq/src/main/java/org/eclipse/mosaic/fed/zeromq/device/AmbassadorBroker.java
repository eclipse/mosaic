package org.eclipse.mosaic.fed.zeromq.device;

import org.eclipse.mosaic.lib.zeromq.bidirectional.AsyncBroker;

public class AmbassadorBroker extends AsyncBroker {

    public AmbassadorBroker(String frontendAddr, String backendAddr) {
        super(frontendAddr, backendAddr);
    }
}
