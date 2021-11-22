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

        log.info("Initialized Zeromq");
    }

    @Override
    protected void processInteraction(Interaction interaction) throws InternalFederateException {
        try {
            if (interaction.getTypeId().startsWith(RsuRegistration.TYPE_ID)) {
                this.process((RsuRegistration) interaction);
            }  else if (interaction.getTypeId().startsWith(VehicleUpdates.TYPE_ID)) {
                this.process((VehicleUpdates) interaction);
            } else if (interaction.getTypeId().equals(V2xMessageTransmission.TYPE_ID)) {
                this.process((V2xMessageTransmission) interaction);
            } else {
                log.warn("Received unknown interaction={} @time={}", interaction.getTypeId(), TIME.format(interaction.getTime()));
            }
        } catch (Exception e) {
            throw new InternalFederateException(e);
        }
    }

    private void process(RsuRegistration interaction) {
        final RsuMapping applicationRsu = interaction.getMapping();
        if (applicationRsu.hasApplication()) {
            SimulationEntities.INSTANCE.createOrUpdateOfflineNode(applicationRsu.getName(), applicationRsu.getPosition().toCartesian());
            log.info("Added RSU id={} position={} @time={}", applicationRsu.getName(), applicationRsu.getPosition(), TIME.format(interaction.getTime()));
        }
    }


    private void process(VehicleUpdates interaction) {
        for (VehicleData added : interaction.getAdded()) {
            addOrUpdateVehicle(added);
        }
        for (VehicleData updated : interaction.getUpdated()) {
            if (addOrUpdateVehicle(updated) && log.isTraceEnabled()) {
                log.trace("Moved Vehicle id={} to position={} @time={}",
                        updated.getName(), updated.getPosition(), TIME.format(interaction.getTime()));
            }
        }
        for (final String removedName : interaction.getRemovedNames()) {
            removeVehicle(removedName);
            log.info("Removed Vehicle id={} @time={}", removedName, TIME.format(interaction.getTime()));
        }
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
