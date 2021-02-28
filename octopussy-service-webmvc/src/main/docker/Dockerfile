ARG JRE_IMAGE=adoptopenjdk/openjdk11:alpine-jre
FROM $JRE_IMAGE

ENV \
JDBC_DRIVER_URLS="" \
JVM_OPTS="-Xmx1536M"

ADD ${lib.dir} /${lib.dir}/
ADD ${dist.file} docker-entrypoint.sh /

ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 8080
