import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.javafmi.wrapper.Simulation;
import org.javafmi.wrapper.v2.Access;

public class fmuApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {
    Simulation fmu;
    Access fmuAccess;

    long currentTime;
    long lastStepTime = 0;

    @Override
    public void onStartup() {
        int startTime = 1;
        int stopTime = 2000;
        fmu = new Simulation("../../Linear_Pos.fmu");
        fmu.init(startTime, stopTime);
        fmuAccess = new Access(fmu);
    }

    @Override
    public void onVehicleUpdated(VehicleData previousVehicleData, VehicleData updatedVehicleData) {
        currentTime = getOs().getSimulationTimeMs();
        long stepSize = currentTime - lastStepTime;
        fmuAccess.doStep(lastStepTime, stepSize);
//        fmuAccess.getRealOutputDerivatives();
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void processEvent(Event event) {
        // ...
    }

    @Override
    public VehicleOperatingSystem getOs() {
        return null;
    }
}