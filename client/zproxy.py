#!/usr/bin/python3
import zmq


class ZmqProxy(object):
    def __init__(self):
        context = zmq.Context()

        # Socket facing server. i.e. Eclipse MOSAIC
        backend = context.socket(zmq.XSUB)
        backend.bind("tcp://127.0.0.1:5321")

        # Socket facing clients. i.e. Python
        frontend = context.socket(zmq.XPUB)
        frontend.bind("tcp://127.0.0.1:6666")

        self.frontend = frontend
        self.backend = backend
        self.context = context

        print('zmq.proxy started...')


def main():
    p = ZmqProxy()
    zmq.proxy(p.frontend, p.backend)
    # We never get here…
    p.frontend.close()
    p.backend.close()
    p.context.term()


if __name__ == "__main__":
    main()
