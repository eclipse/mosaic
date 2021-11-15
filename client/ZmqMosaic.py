#!/usr/bin/python3

import zmq

def main():
    context = zmq.Context()
    subscriber = context.socket(zmq.SUB)
    subscriber.connect("tcp://localhost:5321")
    subscriber.setsockopt_string(zmq.SUBSCRIBE, "")
    print("Subscriber connected to port 5321...")

    while True:
        msg = subscriber.recv_string()
        print(msg)

if __name__ == "__main__":
    main()