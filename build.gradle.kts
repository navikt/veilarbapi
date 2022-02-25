val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val navcommonVersion: String by project
val mockOAuth2ServerVersion: String by project
val tokenValidationVersion: String by project


plugins {
    application
    kotlin("jvm") version "1.6.10"
    `java-library`
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id ("org.openapi.generator") version "5.4.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencyLocking {
    lockAllConfigurations()
}


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
    mainClass.set("no.nav.poao.ApplicationKt")
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
            attributes(Pair("Main-Class", "no.nav.poao.ApplicationKt"))
        }
    }
}

tasks.named( "compileKotlin") {
    dependsOn( "generateAktivitetsplanClient", "generateVeilarbapiServer")
}
java.sourceSets["main"].java.srcDir("$buildDir/generated/src/main/java")
kotlin.sourceSets["main"].kotlin.srcDir("$buildDir/generated/src/main/kotlin")



dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    implementation("no.nav.security:token-validation-ktor:$tokenValidationVersion") {
        exclude(group = "io.ktor", module = "ktor-auth")
    }
    api("javax.validation:validation-api:2.0.1.Final")
    implementation("org.realityforge.javax.annotation:javax.annotation:1.0.1")

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.14")
    // LOGGING
    implementation(group= "ch.qos.logback", name= "logback-classic", version= "1.2.6")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")

    implementation("com.natpryce:konfig:1.6.10.0")

    implementation("no.nav.common:util:$navcommonVersion")
    implementation("no.nav.common:sts:$navcommonVersion")
    // Velarbaktivitet Rest Client START
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-client-java:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    // Azure client
    implementation("io.ktor:ktor-client-jackson:$ktor_version")
    // Rest Client END
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    // avhengigheter i generert server kode
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.gsonfire:gson-fire:1.8.5")
    api("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation(group= "org.threeten", name= "threetenbp", version= "1.5.1")
    implementation("io.swagger:swagger-annotations:1.6.5")
    implementation("com.squareup.okio:okio:3.0.0")
    // avhengigheter i generert client kode
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
 //   implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
//    compileOnly(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.2")
//    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    // avhengigheter i generert kode SLUTT

}
