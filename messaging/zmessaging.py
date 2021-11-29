import zmq

class ZMessaging():

    def __init__(self, frontend: str = "tcp://127.0.0.1:5566"):
        ctx = zmq.Context()

        client = ctx.socket(zmq.DEALER)
        warning = ctx.socket(zmq.DEALER)
        warning_success = ctx.socket(zmq.DEALER)
        client.setsockopt(zmq.IDENTITY, b"req.interaction")
        warning.setsockopt(zmq.IDENTITY, b"service.warning")

        client.connect(frontend)
        warning.connect(frontend)
        poller = zmq.Poller()
        poller_warning = zmq.Poller()

        self.client = client
        self.warning = warning

        poller.register(self.client, zmq.POLLIN)
        poller_warning.register(self.warning, zmq.POLLIN)

        self.poller = poller
        self.poller_warning = poller_warning

    def receive_data(self, timeout: int = 100):
        self.client.send(b"req.interaction", zmq.SNDMORE)
        self.client.send(b"", 0)
        sockets = dict(self.poller.poll(timeout))
        if self.client in sockets:
            msg = self.client.recv_multipart()
            return msg
        else:
            return None

    def send_warning(self, road_id: str, timeout: int = 100):
        self.warning.send_string("service.warning", zmq.SNDMORE)
        self.warning.send_string(road_id, 0)
        sockets = dict(self.poller_warning.poll(timeout))
        if self.warning in sockets:
            ret = self.warning.recv_multipart()
            return ret
        else:
            return None
