#!/usr/bin/python3

import zeromq_interact_pb2 as zeromq_interact
from zeromq_interact_pb2 import _message

class ProtoParser():
    def __init__(self, message):
        msg_proto = message[1] # USE THIS
        msg_proto_bytes = bytearray(msg_proto)

        prototype = zeromq_interact.ZInteract()
        # prototype.FromString(msg_proto)
        print('testtest')
        print('testtest')
        print('testtest')
        print('testtest')
