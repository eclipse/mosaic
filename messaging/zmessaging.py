import zmq

class ZMessaging():

    def __init__(self, frontend: str = "tcp://127.0.0.1:5566"):
        ctx = zmq.Context()

        # Create sockets for subscriber and pipeline
        client = ctx.socket(zmq.DEALER)
        warning = ctx.socket(zmq.DEALER)
        client.setsockopt(zmq.IDENTITY, b"req.interaction")
        warning.setsockopt(zmq.IDENTITY, b"service.warning")

        client.connect(frontend)
        warning.connect(frontend)
        poller = zmq.Poller()

        poller.register(client, zmq.POLLIN)
        poller.register(warning, zmq.POLLIN)

        self.client = client
        self.warning = warning
        self.poller = poller

    def receive_data(self, timeout: int = 100):
        self.client.send_string("")
        sockets = dict(self.poller.poll(timeout))
        if self.client in sockets:
            msg = self.client.recv_json()
            return msg
        else:
            return None

    def send_warning(self, road_id: str, timeout: int = 100):
        self.warning.send_string(road_id)
        sockets = dict(self.poller.poll(timeout))
        if self.warning in sockets:
            ret = self.warning.recv_json()
            return ret
        else:
            return None
