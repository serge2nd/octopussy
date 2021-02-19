ARG JRE_IMAGE=adoptopenjdk/openjdk11:alpine-jre
FROM $JRE_IMAGE

ENV \
EXT_DIR=/ext \
JVM_OPTS=-Xmx1536M

ADD . /
RUN ln -s /docker-entrypoint.sh /usr/local/bin/

ENTRYPOINT ["docker-entrypoint.sh"]
EXPOSE 8080
