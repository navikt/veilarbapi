FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

ENV TZ="Europe/Oslo"
WORKDIR /app
#COPY /build/libs/veilarbapi-all.jar app.jar
COPY build/install/*/lib /lib
COPY src/main/resources/logback.xml /app/logback.xml

EXPOSE 8080
USER nonroot

ENTRYPOINT ["java", "-Dlogback.configurationFile=/app/logback.xml", "-cp", "/lib/*", "no.nav.poao.veilarbapi.ApplicationKt"]