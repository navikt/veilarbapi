FROM busybox:1.36.1-uclibc as busybox

FROM gcr.io/distroless/java21

COPY --from=busybox /bin/sh /bin/sh
COPY --from=busybox /bin/printenv /bin/printenv
COPY --from=busybox /bin/mkdir /bin/mkdir
COPY --from=busybox /bin/chown /bin/chown

ENV TZ="Europe/Oslo"
WORKDIR /app
#COPY /build/libs/veilarbapi-all.jar app.jar
COPY build/install/*/lib /lib
COPY src/main/resources/logback.xml /app/logback.xml

EXPOSE 8080
USER nonroot

ENTRYPOINT ["java", "-Dlogback.configurationFile=/app/logback.xml", "-cp", "/lib/*", "no.nav.poao.veilarbapi.ApplicationKt"]