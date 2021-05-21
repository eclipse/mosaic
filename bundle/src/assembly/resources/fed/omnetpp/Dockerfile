FROM ubuntu:bionic

LABEL \
    description="Docker image containing the MOSAIC adapted OMNeT++ federate" \
    version="${pom.version}" \
    maintainer="mosaic@fokus.fraunhofer.de"

RUN \
    apt-get update && \
    apt-get install -y --allow-unauthenticated \
    build-essential \
    bison \
    flex \
    libprotobuf-dev \
    protobuf-compiler \
    libxml2-dev \
    python \
    unzip \
    wget \
    zlib1g-dev

WORKDIR /home/mosaic/bin/fed/omnetpp

COPY ./*.tgz omnet_installer.sh ./

RUN \
    ls -all && \
    ./omnet_installer.sh -q -t USER -o "/home/mosaic/bin/fed/omnetpp/omnetpp-5.5.1-src-linux.tgz" -i "/home/mosaic/bin/fed/omnetpp/inet-4.1.1-src.tgz" && \
    mkdir -p omnetpp-federate/simulations

VOLUME ["/home/mosaic/bin/fed/omnetpp/omnetpp-federate"]

EXPOSE 40001 40002

ENTRYPOINT \
    cp omnetpp-federate/src/omnetpp.ini omnetpp-federate/simulations/ && \
    omnetpp-federate/omnetpp-federate -u Cmdenv \
    -f omnetpp-federate/simulations/omnetpp.ini \
    -n "inet:omnetpp-federate/src" \
    --mosaiceventscheduler-host=0.0.0.0 \
    --mosaiceventscheduler-port=40001 \
    --mosaiccmd-port=40002
