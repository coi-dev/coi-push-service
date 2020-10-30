FROM openjdk:11.0.5-jre-slim-buster
LABEL maintainer="secret identity"

ARG COI_PUSH_HOME=/opt/coi-push-service
ENV COI_PUSH_HOME=${COI_PUSH_HOME}
ENV COI_PUSH_CONFIG_FILE=${COI_PUSH_HOME}/etc/pushservice.properties

COPY COIPushService/build/libs/*.jar ${COI_PUSH_HOME}/
COPY Documentation/build/sphinx/ /docu/
WORKDIR ${COI_PUSH_HOME}

EXPOSE 8080

VOLUME "${COI_PUSH_HOME}/logs"

CMD [ "/bin/bash", "-c", "java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.bin -Xmx2G -Dspring.config.location=etc/pushservice.properties -jar ${COI_PUSH_HOME}/*.jar" ]
