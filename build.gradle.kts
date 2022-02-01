val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id ("org.openapi.generator") version "5.3.0"
}

group = "no.nav.poao"
version = "0.0.1"
application {
    mainClass.set("no.nav.poao.ApplicationKt")
}

repositories {
    mavenCentral()
}

task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateAktivitetsplanClient") {
    id.set("aktivitetsplan-client")
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/resources/openapi/AktivitetsplanV1.yaml")
    groupId.set("no.nav.veilarbaktivitet")
    packageName.set("no.nav.veilarbaktivitet.client")
    outputDir.set("$buildDir/generated")
}

task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateArbeidsoppfolgingServer") {
    id.set("arbeidsoppfolging-server")
    generatorName.set("kotlin-server")
    library.set("ktor")
    inputSpec.set("$projectDir/src/main/resources/openapi/ArbeidsoppfolgingV1.yaml")
    groupId.set("no.nav.veilarbapi")
    packageName.set("no.nav.veilarbapi.server")
    outputDir.set("$buildDir/generated")
}

tasks.named( "compileKotlin") {
    dependsOn( "generateAktivitetsplanClient", "generateArbeidsoppfolgingServer")
}

dependencies {
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
