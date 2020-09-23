/*
 * Copyright (c) 2020 Fraunhofer FOKUS and others. All rights reserved.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.mosaic.fed.application.ambassador;

/**
 * The application ambassador has a given error range from
 * {@code 0x01000000} to {@code 0x02000000}.
 */
public enum ErrorRegister {

    // 0x01000000 to 0x0100000F configuration issues
    CONFIGURATION_CouldNotReadFromFile(0x01000000, "Configuration object could not be instantiated."),
    // 0x01000010 to 0x0100001F ambassador issues
    AMBASSADOR_UncaughtExceptionInProcessInteraction(0x01000010, "Uncaught exception during process interaction."),
    AMBASSADOR_RequestingAdvanceTime(0x01000011, "Error requesting advance time."),
    AMBASSADOR_ErrorLoadingJarFiles(0x01000012, "There was an error loading the applications."),
    AMBASSADOR_ErrorAdvanceTime(0x01000013, "There was an error during an advance time."),
    AMBASSADOR_ErrorSendInteraction(0x01000014, "There was an error during sending an interaction."),
    AMBASSADOR_ErrorCalculateDeparture(0x01000015, "Could not calculate departure for vehicle."),
    // 0x01000020 to 0x0100002F simulation kernel
    SIMULATION_KERNEL_ConfigurationNotSet(0x01000020, "The configuration was not set."),
    SIMULATION_KERNEL_ConfigurationAlreadySet(0x01000021, "The configuration was already set."),
    SIMULATION_KERNEL_EventManagerNotSet(0x01000022, "The EventManager was not set."),
    SIMULATION_KERNEL_EventManagerAlreadySet(0x01000023, "The EventManager was already set."),
    SIMULATION_KERNEL_InteractableNotSet(0x01000024, "The Interactable was not set."),
    SIMULATION_KERNEL_InteractableAlreadySet(0x01000025, "The Interactable was already set."),
    SIMULATION_KERNEL_ClassLoaderNotSet(0x01000091, "The ClassLoader was not set."),
    SIMULATION_KERNEL_ClassLoaderAlreadySet(0x01000092, "The ClassLoader was already set."),
    SIMULATION_KERNEL_RandomNumberGeneratorNotSet(0x01000093, "The RandomNumberGenerator was not set."),
    SIMULATION_KERNEL_RandomNumberGeneratorAlreadySet(0x01000094, "The RandomNumberGenerator was already set."),
    SIMULATION_KERNEL_ConfigurationPathNotSet(0x01000026, "The configuration path was not set."),
    SIMULATION_KERNEL_ConfigurationPathAlreadySet(0x01000027, "The configuration path was already set."),
    SIMULATION_KERNEL_CentralNavigationComponentNotSet(0x01000028, "The CentralNavigationComponent was not set."),
    SIMULATION_KERNEL_CentralNavigationComponentAlreadySet(0x01000029, "The CentralNavigationComponent was already set."),
    // 0x01000030 to 0x0100003F unit simulator
    UNIT_SIMULATOR_IdAlreadyAssigned(0x01000030, "The id is already assigned."),
    UNIT_SIMULATOR_IdFromUnitIsNotInMap(0x01000031, "The unit with the id couldn't be found."),
    UNIT_SIMULATOR_AddedUnitInPreviousTime(0x01000032, "The unit was added in a previous time."),
    UNIT_SIMULATOR_UnknownSimulationUnitToStartApplications(0x01000033, "Unknown SimulationUnit type to start applications."),
    UNIT_SIMULATOR_UnknownSimulationUnitToPutInMap(0x01000034, "Unknown SimulationUnit type to put in map."),
    UNIT_SIMULATOR_UnknownSimulationUnitToRemoveFromMap(0x01000035, "Unknown SimulationUnit type to remove from map."),
    // 0x01000040 to 0x0100004F simulation unit
    SIMULATION_UNIT_UncaughtExceptionDuringProcessEvent(0x01000040, "Uncaught exception during process event in an application."),
    SIMULATION_UNIT_ClassNotFoundException(0x01000041, "Class could not be found."),
    SIMULATION_UNIT_ConstructorNotFoundError(0x01000042, "Constructor could not be found or called."),
    SIMULATION_UNIT_IsNotAssignableFrom(0x01000043, "Class found, but the class does not extend the specific Application class."),
    // 0x01000050 to 0x0100005F charging station
    CHARGING_STATION_UnknownEvent(0x01000050, "Process unknown event."),
    CHARGING_STATION_NoEventResource(0x01000051, "Process event with no resource."),
    // 0x01000060 to 0x0100006F road side unit
    ROAD_SIDE_UNIT_UnknownEvent(0x01000060, "Process unknown event."),
    ROAD_SIDE_UNIT_NoEventResource(0x01000061, "Process event with no resource."),
    // 0x01000070 to 0x0100007F traffic light
    TRAFFIC_LIGHT_UnknownEvent(0x01000070, "Process unknown event."),
    TRAFFIC_LIGHT_NoEventResource(0x01000071, "Process event with no resource."),
    // 0x01000080 to 0x0100008F vehicle
    VEHICLE_UnknownEvent(0x01000080, "Process unknown event."),
    VEHICLE_NoEventResource(0x01000081, "Process event with no resource.");

    /**
     * The code of the error.
     */
    private final int code;

    /**
     * The description of the error.
     */
    private final String description;

    /**
     * Construct a new error.
     *
     * @param code        the code of the error.
     * @param description the description of the error.
     */
    ErrorRegister(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public long getCode() {
        return code;
    }

    @Override
    public String toString() {
        final String hexString = Integer.toHexString(code);
        final int leadingZeros = 8;
        final StringBuilder sb = new StringBuilder(leadingZeros);
        // prepend leading zeros to the string
        for (int i = 0; i < leadingZeros - hexString.length(); i++) {
            sb.append("0");
        }
        sb.append(hexString);
        return "Error code: 0x" + sb.toString() + ": " + description;
    }
}
