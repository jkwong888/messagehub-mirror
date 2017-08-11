FROM openjdk:8-alpine

RUN apk add --no-cache bash wget ca-certificates jq \
 && update-ca-certificates

ADD http://www-us.apache.org/dist/kafka/0.10.2.1/kafka_2.10-0.10.2.1.tgz /

COPY /scripts/ /scripts
COPY /config/ /config

RUN chmod a+x /scripts/*

CMD [ "/kafka_2.10-0.10.2.1/bin/kafka-mirror-maker.sh" ]
ENTRYPOINT [ "/scripts/entrypoint.sh" ]


