FROM alpine/java:21 as build
ENV SPRING_PROFILES_ACTIVE docker
RUN mkdir -p /src
COPY . /src
RUN chmod -R 777 /src
WORKDIR /src

RUN ./gradlew --no-daemon --configure-on-demand clean bootJar --stacktrace

EXPOSE 8080
RUN cp build/libs/Bank*exec.jar /usr/local/bin/service.jar
CMD ["java", "-jar", "/usr/local/bin/service.jar"]

