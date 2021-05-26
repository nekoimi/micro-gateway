FROM openjdk:17-jdk-alpine

LABEL maintainer="nekoimi <nekoimime@gmail.com>"

ARG JAR_PATH=""
ARG PROFILE_ACTIVE=test
ARG JAR_NAME="app.jar"
ARG WORK_HOME="/work"

ENV TZ=Asia/Shanghai
ENV JAVA_OPTIONS=""
ENV JAR_PATH=${JAR_PATH}
ENV JAR_NAME=${JAR_NAME}
ENV PROFILE_ACTIVE=${PROFILE_ACTIVE}

WORKDIR $WORK_HOME
COPY $JAR_PATH/target/$JAR_NAME  $WORK_HOME/
VOLUME /var/java/data

EXPOSE 8080
CMD java $JAVA_OPTIONS -jar -Dspring.profiles.active=$PROFILE_ACTIVE $JAR_NAME