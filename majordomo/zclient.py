import zmq
from ZmqProtoToolbox import Stream


def main():
    context = zmq.Context()
    subscriber = context.socket(zmq.SUB)
    subscriber.subscribe('')
    subscriber.connect("tcp://127.0.0.1:6666")
    print("Subscriber connected to port 6666...")

    streamer = Stream(1024)

    while True:
        msg = subscriber.recv_multipart()
        data = streamer.receive_stream_data(msg)
        print(data)

if __name__ == '__main__':
    main()
