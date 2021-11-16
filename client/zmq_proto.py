import zeromq_interact_pb2
import zmq


class ProtoParser():
    def __init__(self, typeId: str, message):
        