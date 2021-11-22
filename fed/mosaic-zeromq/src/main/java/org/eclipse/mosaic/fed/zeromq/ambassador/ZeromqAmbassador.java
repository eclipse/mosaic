package org.eclipse.mosaic.fed.zeromq.ambassador;

import org.eclipse.mosaic.interactions.communication.AdHocCommunicationConfiguration;
import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.AbstractFederateAmbassador;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.mapping.ChargingStationMapping;
import org.eclipse.mosaic.lib.objects.mapping.RsuMapping;
import org.eclipse.mosaic.lib.objects.mapping.TrafficLightMapping;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.eclipse.mosaic.fed.zeromq.config.CZeromq;
import org.eclipse.mosaic.lib.zeromq.majordomo.MajordomoBroker;
import org.eclipse.mosaic.lib.zeromq.majordomo.MajordomoWorker;
import org.eclipse.mosaic.lib.zeromq.majordomo.MajordomoClient;
import org.eclipse.mosaic.lib.zeromq.majordomo.MDP;

import java.util.Arrays;


public class ZeromqAmbassador extends AbstractFederateAmbassador {

    protected ZeromqAmbassador(AmbassadorParameter ambassadorParameter) {
        super(ambassadorParameter);
        //TODO Auto-generated constructor stub
    }


    @Override
    public void initialize(final long startTime, final long endTime) throws InternalFederateException {
        super.initialize(startTime, endTime);
        this.log.info("Init simulation with startTime={}, endTime={}", startTime, endTime);

        if (log.isTraceEnabled()) {
            log.trace("subscribedMessages: {}", Arrays.toString(this.rti.getSubscribedInteractions().toArray()));
        }

        try {
            CZeromq configuration = new ObjectInstantiation<>(CZeromq.class).readFile(ambassadorParameter.configuration);
        } catch (InstantiationException e) {
            log.error("Could not read configuration. Reason: {}", e.getMessage());
        }

        log.info("Initialized SNS");
    }

    @Override
    public boolean isTimeConstrained() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTimeRegulating() {
        // TODO Auto-generated method stub
        return false;
    }
    
}
