FROM ubuntu:22.04

RUN apt-get update && apt-get upgrade -y

RUN apt-get install -y maven wget openjdk-17-jdk
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/

WORKDIR /algorithmes-de-parcoursup/

ENV DOCKERIZE_VERSION v0.9.3
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

COPY ./ /algorithmes-de-parcoursup/
#RUN mvn clean -P ordreAppel-executable compile assembly:single
