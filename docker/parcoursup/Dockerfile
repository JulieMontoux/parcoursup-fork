FROM ubuntu:18.04

RUN apt-get update && apt-get upgrade -y

RUN apt-get install -y maven wget openjdk-11-jdk
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/

WORKDIR /algorithmes-de-parcoursup/

ENV DOCKERIZE_VERSION v0.6.1
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

COPY ./ /algorithmes-de-parcoursup/
#RUN mvn clean -P ordreAppel-executable compile assembly:single
