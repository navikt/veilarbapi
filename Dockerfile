FROM docker.pkg.github.com/navikt/pus-nais-java-app/pus-nais-java-app:java17

COPY /build/libs/veilarbapi-all.jar app.jar
