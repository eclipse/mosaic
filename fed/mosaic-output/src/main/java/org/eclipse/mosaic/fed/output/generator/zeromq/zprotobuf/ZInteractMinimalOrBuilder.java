// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: zeromq_interact.proto

package org.eclipse.mosaic.fed.output.generator.zeromq.zprotobuf;

public interface ZInteractMinimalOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ZInteractSpace.ZInteractMinimal)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 time = 1;</code>
   * @return The time.
   */
  long getTime();

  /**
   * <code>int64 id = 2;</code>
   * @return The id.
   */
  long getId();

  /**
   * <code>string sender_id = 3;</code>
   * @return The senderId.
   */
  java.lang.String getSenderId();
  /**
   * <code>string sender_id = 3;</code>
   * @return The bytes for senderId.
   */
  com.google.protobuf.ByteString
      getSenderIdBytes();

  /**
   * <code>string type_id = 4;</code>
   * @return The typeId.
   */
  java.lang.String getTypeId();
  /**
   * <code>string type_id = 4;</code>
   * @return The bytes for typeId.
   */
  com.google.protobuf.ByteString
      getTypeIdBytes();

  /**
   * <code>.ZInteractSpace.ZVehicleUpdatesMinimal vehicle_updates = 5;</code>
   * @return Whether the vehicleUpdates field is set.
   */
  boolean hasVehicleUpdates();
  /**
   * <code>.ZInteractSpace.ZVehicleUpdatesMinimal vehicle_updates = 5;</code>
   * @return The vehicleUpdates.
   */
  org.eclipse.mosaic.fed.output.generator.zeromq.zprotobuf.ZVehicleUpdatesMinimal getVehicleUpdates();
  /**
   * <code>.ZInteractSpace.ZVehicleUpdatesMinimal vehicle_updates = 5;</code>
   */
  org.eclipse.mosaic.fed.output.generator.zeromq.zprotobuf.ZVehicleUpdatesMinimalOrBuilder getVehicleUpdatesOrBuilder();
}
