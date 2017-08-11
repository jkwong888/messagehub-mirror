#!/bin/bash

# parse the source hub json
src_bootstrap_servers=`echo ${SRC_MESSAGE_HUB} | jq -r '.kafka_brokers_sasl | join(",")'`
src_username=`echo ${SRC_MESSAGE_HUB} | jq -r '.user'`
src_password=`echo ${SRC_MESSAGE_HUB} | jq -r '.password'`
src_instanceid=`echo ${SRC_MESSAGE_HUB} | jq -r '.instance_id'`

# parse the destination hub json
dst_bootstrap_servers=`echo ${DST_MESSAGE_HUB} | jq -r '.kafka_brokers_sasl | join(",")'`
dst_username=`echo ${DST_MESSAGE_HUB} | jq -r '.user'`
dst_password=`echo ${DST_MESSAGE_HUB} | jq -r '.password'`
dst_instanceid=`echo ${DST_MESSAGE_HUB} | jq -r '.instance_id'`

# consumer properties (src)
sed -i \
    -e 's/^group.id=.*$/group.id=messagehub-mirror-'${src_instanceid}'/' \
    -e 's/^client.id=.*$/client.id=messagehub-mirror-'${src_instanceid}'/' \
    -e 's/^bootstrap.servers=.*$/bootstrap.servers='${src_bootstrap_servers}'/' \
    -e 's/^sasl.jaas.config=.*$/sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="'${src_username}'" password="'${src_password}'";/' \
    /config/consumer.properties

# producer properties (src)
sed -i \
    -e 's/^group.id=.*$/group.id=messagehub-mirror-'${dst_instanceid}'/' \
    -e 's/^client.id=.*$/client.id=messagehub-mirror-'${dst_instanceid}'/' \
    -e 's/^bootstrap.servers=.*$/bootstrap.servers='${dst_bootstrap_servers}'/' \
    -e 's/^sasl.jaas.config=.*$/sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="'${dst_username}'" password="'${dst_password}'";/' \
    /config/producer.properties

MIRROR_MAKER_ARGS=""
MIRROR_MAKER_ARGS="${MIRROR_MAKER_ARGS} --consumer.config /config/consumer.properties"
MIRROR_MAKER_ARGS="${MIRROR_MAKER_ARGS} --producer.config /config/producer.properties"
MIRROR_MAKER_ARGS="${MIRROR_MAKER_ARGS} --whitelist ${TOPIC}"

exec $@ ${MIRROR_MAKER_ARGS}
