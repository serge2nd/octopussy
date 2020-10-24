ARG JRE_IMAGE=adoptopenjdk/openjdk11:jre-11.0.8_10-alpine
FROM $JRE_IMAGE
ENV EXT_DIR=/ext

ADD ../../../../src/main/java /
RUN ln -s /docker-entrypoint.sh /usr/local/bin/

ENTRYPOINT ["docker-entrypoint.sh"]
EXPOSE 8080
