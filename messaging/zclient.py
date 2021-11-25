import zmq


def main():
    context = zmq.Context()
    subscriber = context.socket(zmq.SUB)
    pusher_warning = context.socket(zmq.PUSH)
    subscriber.subscribe('')
    subscriber.connect("tcp://127.0.0.1:6666")
    pusher_warning.connect("tcp://127.0.0.1:2222")
    print("Subscriber connected to port 6666...")
    print("Pusher warning connected to port 2222...")

    while True:
        msg = subscriber.recv_json()
        print(msg)


if __name__ == '__main__':
    main()
