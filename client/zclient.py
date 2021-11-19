import zmq
from ZmqProtoToolbox import LiveSerDes


def main():
    context = zmq.Context()
    subscriber = context.socket(zmq.SUB)
    subscriber.subscribe('')
    subscriber.connect("tcp://127.0.0.1:6666")
    print("Subscriber connected to port 6666...")

    while True:
        msg = subscriber.recv_multipart()
        live_data = LiveSerDes(msg)
        print(msg)


if __name__ == '__main__':
    main()
