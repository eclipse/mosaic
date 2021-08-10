#!/usr/bin/env bash

(cd .. && make config=debug clean && make config=debug)

if [ ! -f "./federate_dbg" ] && [ -f "../bin/Debug/omnetpp-federate" ]; then
   ln -s "../bin/Debug/omnetpp-federate" "federate_dbg"
fi
if [ ! -f "./federate" ] && [ -f "../bin/Debug/omnetpp-federate" ]; then
   ln -s "../bin/Debug/omnetpp-federate" "federate"
fi
if [ ! -f "./libINET_dbg.so" ]; then
   ln -s "../../inet_src/src/libINET_dbg.so" "libINET_dbg.so"
fi
