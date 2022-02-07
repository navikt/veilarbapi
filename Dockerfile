FROM docker.pkg.github.com/navikt/pus-nais-java-app/pus-nais-java-app:java17
COPY ./build/install/veilarbapi/ .
WORKDIR /bin
CMD ["./veilarbapi"]
