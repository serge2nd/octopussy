ARG JRE_IMAGE=adoptopenjdk/openjdk11:alpine-jre
FROM $JRE_IMAGE

ENV \
# Links to download extensions, e.g. JDBC drivers (separated by whitespaces)
EXT_URLS="" \
# JVM options
JVM_OPTS="-Xmx1536M"

ADD ${lib.dir} /${lib.dir}/
ADD ${dist.file} docker-entrypoint.sh /

ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 8080
