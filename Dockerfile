FROM ghcr.io/navikt/poao-baseimages/java:17

COPY /build/libs/veilarbapi-all.jar app.jar
