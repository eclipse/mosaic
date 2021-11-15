import zmq

from zeromq_interact_pb2 import ZInteract


def main():
    _SUBSCRIBE_B = b'1'
    _UNSUBSCRIBE_B = b'0'
    _TOPIC = b'VehicleUpdates'

    _SUB_MSG = _SUBSCRIBE_B + _TOPIC

    context = zmq.Context()
    subscriber = context.socket(zmq.SUB)
    subscriber.setsockopt_string(zmq.SUBSCRIBE, "VehicleUpdates")
    subscriber.connect("tcp://localhost:5322")
    print("Subscriber connected to port 5322...")

    while True:
        msg = subscriber.recv()
        print(msg)


if __name__ == "__main__":
    main()
