#!/bin/bash

JAVA_OPTS="-Djava.security.egd=file:///dev/urandom"
CATALINA_OPTS="-Xms512M -Xmx2048M -server -XX:+UseParallelGC"

#
# Edit conf/catalina.properties according to given environment 
#

if [ -n "${DB_HOST}" ]; then
    sed -i -e "s~^db[.]host[ ]*=[ ]*.*$~db.host=${DB_HOST}~" conf/catalina.properties 
fi

if [ -n "${DB_PORT}" ]; then
    sed -i -e "s~^db[.]port[ ]*=[ ]*.*$~db.port=${DB_PORT}~" conf/catalina.properties 
fi

if [ -n "${DB_NAME}" ]; then
    sed -i -e "s~^db[.]name[ ]*=[ ]*.*$~db.name=${DB_NAME}~" conf/catalina.properties 
fi

if [ -n "${DB_USERNAME}" ]; then
    sed -i -e "s~^db[.]username[ ]*=[ ]*.*$~db.username=${DB_USERNAME}~" conf/catalina.properties 
fi

if [ -f "${DB_PASSWORD_FILE}" ]; then
    sed -i -e "s~^db[.]password[ ]*=[ ]*.*$~db.password=$(cat ${DB_PASSWORD_FILE})~" conf/catalina.properties 
fi

#
# Run
#

export JAVA_OPTS
export CATALINA_OPTS

exec ./bin/catalina.sh run
