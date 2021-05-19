FROM maven:3.6.3-adoptopenjdk-8

ENV DEBIAN_FRONTEND=noninteractive
ENV USER_NAME=jenkins
ENV HOME=/home/jenkins
WORKDIR /home/jenkins

RUN apt-get update && \
    apt-get install -y --allow-unauthenticated software-properties-common && \
    add-apt-repository ppa:sumo/stable && \
    apt-get install -y sumo