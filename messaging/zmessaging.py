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

    def request_data(self):
        msg = self.subscriber.recv_json()
        return msg

    def send_warning(self, road_id: str):
        # ping
        self.publisher.send_string("warning", zmq.SNDMORE)
        self.publisher.send_string(road_id, 0)
        self.send_flag = True

    def ret_warning(self):
        # pong
        socks = dict(self.poller.poll(1000))
        if self.puller in socks:
            msg = self.puller.recv_multipart()
            return msg
        else:
            return False
