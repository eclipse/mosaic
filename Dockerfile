#syntax=docker/dockerfile:1

FROM maven:3-openjdk-11

RUN apt update && apt install -y software-properties-common

RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 87637B2A34012D7A
RUN add-apt-repository "deb http://ppa.launchpad.net/sumo/stable/ubuntu focal Release" -y
RUN apt update && apt install sumo sumo-tools sumo-doc -y

RUN useradd -ms /bin/bash mosaic
USER mosaic
WORKDIR /home/mosaic

EXPOSE 8443
EXPOSE 8888

CMD /bin/bash