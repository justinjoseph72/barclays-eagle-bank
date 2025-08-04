FROM alpine/java:21
RUN mkdir -p /src
#RUN groupadd --system --gid 1000 postgres
#RUN #useradd --system --gid postgres --uid 100 --shell /bin/sh --create-home postgres
COPY . /src
RUN chmod -R 777 /src
#USER postgres
WORKDIR /src

RUN ./gradlew --no-daemon --configure-on-demand clean bootJar --stacktrace

