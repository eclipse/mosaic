#!/usr/bin/python3

from pymdp.mdp_api import MajorDomoClient


class MdpClient(object):
    def __init__(self, port: int, verbose: bool = False) -> None:
        super().__init__()
        broker_address = "tcp://127.0.0.1:" + str(port)
        client = MajorDomoClient(broker_address, verbose)
        self.client = client

    def request_service(self, service: bytes, request: int):
        for i in range(request):
            self.client.send(service=service, request=b"")

    def receive_reply(self):
        reply = self.client.recv()
        return reply


def main():
    service_veh = b"vehicle"
    service_warning = b"warning"

    mdp = MdpClient(port=5555, verbose=False)
    mdp.request_service(service_veh, 5)
    while True:
        reply = mdp.receive_reply()
        print(reply)


if __name__ == '__main__':
    main()
