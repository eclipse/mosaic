#!/usr/bin/python3

"""
Majordomo Protocol broker
A minimal implementation of http:#rfc.zeromq.org/spec:7 and spec:8

Author: Min RK <benjaminrk@gmail.com>
Based on Java example by Arkadiusz Orzechowski
"""

from pymdp.mdp_api import MajorDomoBroker


def start_broker():
    mdbroker = MajorDomoBroker(True)
    mdbroker.bind("tcp://127.0.0.1:5555")
    mdbroker.mediate()


def main():
    start_broker()


if __name__ == "__main__":
    main()
