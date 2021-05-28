package org.eclipse.mosaic.lib.objects;

public enum UnitType {

    VEHICLE("veh"),
    ROAD_SIDE_UNIT("rsu"),
    TRAFFIC_MANAGEMENT_CENTER("tmc"),
    TRAFFIC_LIGHT("tl"),
    CHARGING_STATION("cs"),
    SERVER("server");

    public final String prefix;

    UnitType(String prefix) {
        this.prefix = prefix;
    }
}