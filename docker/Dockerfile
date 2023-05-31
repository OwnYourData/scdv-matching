FROM openjdk:18
MAINTAINER "Christoph Fabianek" christoph@ownyourdata.eu

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY docker/*.jar /usr/src/app/

CMD java -jar *.jar

EXPOSE 2806
