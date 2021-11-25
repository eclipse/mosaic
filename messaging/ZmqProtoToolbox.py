#!/usr/bin/python3

import zeromq_interact_pb2 as zeromq_interact
from zeromq_interact_pb2 import _message


class LiveSerDes():
    def __init__(self, message):
        ser_msg = message[1]  # USE THIS
        # msg_proto_bytes = bytearray(msg_proto)

        prototype = zeromq_interact.ZInteract()

        des_msg, type_id = self.deserialize(prototype, ser_msg)
        self.deserialize_content(des_msg, type_id)

        self.ser_msg = ser_msg
        self.prototype = prototype

    def deserialize_content(self, des_msg, type_id):
        content = dict()
        if type_id == 'V2xMessageReception':
            content['generic'] = des_msg.z_v2x_message_reception
        elif type_id == 'RsuRegistration':
            content['generic'] = des_msg.z_rsu_registration
        elif type_id == 'VehicleRegistration':
            content['generic'] = des_msg.z_vehicle_registration
        elif type_id == 'VehicleUpdates':
            content['generic'] = des_msg.z_vehicle_updates
            content['sub_added'] = des_msg.z_vehicle_updates.added
            content['sub_updated'] = des_msg.z_vehicle_updates.updated

        self.content = content
        # self._deserialize_subcontent(content_p, type_id)

    def deserialize(self, prototype, ser_msg):
        des_msg = prototype.FromString(ser_msg)
        type_id = des_msg.type_id
        return des_msg, type_id

    def get_deserialized_message(self):
        return self.decoded_msg

    def get_serialized_message(self):
        return self.ser_msg

    def get_prototype(self):
        return self.prototype
