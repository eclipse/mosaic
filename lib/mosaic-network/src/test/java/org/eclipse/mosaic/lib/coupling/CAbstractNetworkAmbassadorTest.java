package org.eclipse.mosaic.lib.coupling;

import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.junit.Test;

public class CAbstractNetworkAmbassadorTest {

    @Test
    public void tiergartenOmnetpp() throws InstantiationException {
        new ObjectInstantiation<>(CAbstractNetworkAmbassador.class)
                .read(getClass().getResourceAsStream("/Tiergarten/omnetpp_config.json"));
    }

    @Test
    public void tiergartenNs3() throws InstantiationException {
        new ObjectInstantiation<>(CAbstractNetworkAmbassador.class)
                .read(getClass().getResourceAsStream("/Tiergarten/ns3_config.json"));
    }
}
