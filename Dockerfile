FROM ubuntu:jammy-20240530 as base
LABEL \
    description="Docker image for executing MOSAIC simulations. (Only contains prerequisites)" \
    version="${pom.version}" \
    maintainer="mosaic@fokus.fraunhofer.de"

# Set Time Zone
ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ARG JAVA_VERSION=11
ARG MAVEN_VERSION=3.9.7
ARG SUMO_VERSION=1.20.0-1
ARG MAVEN_BASE_URL=https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/

# Prerequisites
RUN apt-get update && \
    apt-get install -y software-properties-common && \
    rm -rf /var/lib/apt/lists/*

# Install Dependencies
RUN apt-add-repository -y ppa:sumo/stable \
    && apt-get update \
    && apt-get install -y \
    openjdk-${JAVA_VERSION}-jdk \
    sumo=${SUMO_VERSION} \
    wget \
    git \
    && rm -rf /var/lib/apt/lists/*

# Install Maven
RUN mkdir -p /opt/maven /tmp/mvn \
    && wget -q -O /tmp/mvn/apache-maven.tar.gz ${MAVEN_BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && tar -xzf /tmp/mvn/apache-maven.tar.gz -C /opt/maven --strip-components=1 \
    && rm -r /tmp/mvn/apache-maven.tar.gz \
    && rmdir /tmp/mvn \
    && ln -s /opt/maven/bin/mvn /usr/bin/mvn

# Set Environment Variables
ENV SUMO_HOME=/usr/share/sumo
ENV JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk-amd64
ENV MAVEN_HOME=/opt/mvn
ENV MAVEN_CONFIG="$USER_HOME_DIR/.m2"