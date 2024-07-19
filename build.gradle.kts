val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val navcommonVersion: String by project
val mockOAuth2ServerVersion: String by project
val token_support_version: String by project
val caffeine_version: String by project
val logstashEncoderVersion: String by project
val prometeus_version: String by project


plugins {
    application
    kotlin("jvm") version "1.6.21"
    `java-library`
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.21"
    id ("org.openapi.generator") version "5.4.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.sonarqube") version "4.4.1.3373"
    id ("jacoco")
}

sonarqube {
    properties {
        property ("sonar.projectKey", "navikt_veilarbapi")
        property ("sonar.organization", "navikt")
        property ("sonar.host.url", "https://sonarcloud.io")
    }
}

//tasks.sonarqube {
//    dependsOn(tasks.jacocoTestReport)
//}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

dependencyLocking {
    lockAllConfigurations()
}

configurations.all { resolutionStrategy.failOnVersionConflict() }

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "no.nav.poao"
version = ""
application {
    mainClass.set("no.nav.poao.veilarbapi.ApplicationKt")
}

repositories {
    mavenCentral()
}


task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateAktivitetsplanClient") {
    generatorName.set("java")
    library.set("okhttp-gson-nextgen")
    inputSpec.set("$projectDir/src/main/resources/openapi/AktivitetsplanV1.yaml")
    packageName.set("no.nav.veilarbaktivitet.client")
    apiPackage.set("no.nav.veilarbaktivitet.api")
    modelPackage.set("no.nav.veilarbaktivitet.model")
    outputDir.set("$buildDir/generated")
}

task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateDialogClient") {
    generatorName.set("java")
    library.set("okhttp-gson-nextgen")
    inputSpec.set("$projectDir/src/main/resources/openapi/DialogV1.yaml")
    packageName.set("no.nav.veilarbdialog.client")
    apiPackage.set("no.nav.veilarbdialog.api")
    modelPackage.set("no.nav.veilarbdialog.model")
    outputDir.set("$buildDir/generated")
}

task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateVeilarbapiServer") {
    generatorName.set("java")
    library.set("okhttp-gson-nextgen")
    inputSpec.set("$projectDir/src/main/resources/openapi/ArbeidsoppfolgingV1.yaml")
    groupId.set("no.nav.veilarbaktivitet")
    packageName.set("no.nav.veilarbapi.client")
    apiPackage.set("no.nav.veilarbapi.api")
    modelPackage.set("no.nav.veilarbapi.model")
    globalProperties.put("models", "")
    globalProperties.put("supportingFiles", "JSON.java,AbstractOpenApiSchema.java,ApiException.java")

    outputDir.set("$buildDir/generated")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "no.nav.poao.veilarbapi.ApplicationKt"))
        }
    }
}

tasks.named( "compileKotlin") {
    dependsOn(  "generateDialogClient", "generateAktivitetsplanClient", "generateVeilarbapiServer")
}
java.sourceSets["main"].java.srcDir("$buildDir/generated/src/main/java")
kotlin.sourceSets["main"].kotlin.srcDir("$buildDir/generated/src/main/kotlin")



dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("io.ktor:ktor-server-metrics:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-serialization-gson:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeine_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    implementation("no.nav.security:token-validation-ktor:$token_support_version")
    implementation("no.nav.security:token-client-core:$token_support_version")
    api("javax.validation:validation-api:2.0.1.Final")
    implementation("org.realityforge.javax.annotation:javax.annotation:1.0.1")

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.14")
    // LOGGING
    implementation(group= "ch.qos.logback", name= "logback-classic", version= logback_version)
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    // Monitoring
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")


    implementation("com.natpryce:konfig:1.6.10.0")

    implementation("no.nav.common:util:$navcommonVersion")
    implementation("no.nav.common:sts:$navcommonVersion")
    // Velarbaktivitet Rest Client START
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-client-java:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    // Azure client
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    // Rest Client END
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    // avhengigheter i generert server kode
    implementation("io.gsonfire:gson-fire:1.8.5")
    api("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation(group= "org.threeten", name= "threetenbp", version= "1.5.1")
    implementation("io.swagger:swagger-annotations:1.6.5")
    implementation("com.squareup.okio:okio:3.0.0")
    // avhengigheter i generert client kode
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    // avhengigheter i generert kode SLUTT

    testImplementation("com.github.tomakehurst:wiremock-jre8:2.33.1")
}

