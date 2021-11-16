#!/usr/bin/python3

import zeromq_interact_pb2 as zeromq_interact

class ProtoParser():
    def __init__(self, message):
        self.message = message
        id = message[0].decode(encoding="utf-8")
        # msg_field = message[1].decode(encoding="utf-8")
        self._current_message(id)

    def deserialize(self, byte_message, proto_type):
        module_, class_ = proto_type.rsplit('.', 1)
        class_ = getattr(import_module(module_), class_)
        rv = class_()
        rv.ParseFromString(byte_message)
        return rv

    def _current_message(self,
                         id: str
                         # msg_field: str
                         ):

        prototype = zeromq_interact.ZInteract()
        print('test')
