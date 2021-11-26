import zmq


class ZMessaging():

    def __init__(self,
                 subscriber_port: int = 6666,
                 dealer_port: int = 2222):
        ctx = zmq.Context()

        # Create sockets for subscriber and pipeline
        subscriber = ctx.socket(zmq.SUB)
        dealer = ctx.socket(zmq.DEALER)

        subsc_addr = "tcp://127.0.0.1:" + str(subscriber_port)
        push_addr = "tcp://127.0.0.1:" + str(dealer_port)


        subscriber.connect(subsc_addr)
        publisher.connect(pub_addr)
        pusher.connect(push_addr)
        puller.connect(pull_addr)

        poller = zmq.Poller()
        poller.register(pull_addr, zmq.POLLIN)

        self.subscriber = subscriber
        self.publisher = publisher
        self.puller = puller
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
