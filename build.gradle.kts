val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    `java-library`
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id ("org.openapi.generator") version "5.4.0"
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
version = "0.0.1"
application {
    mainClass.set("no.nav.poao.ApplicationKt")
}

repositories {
    mavenCentral()
}

//
//task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateAktivitetsplanClient") {
//    generatorName.set("java")
//    inputSpec.set("$projectDir/src/main/resources/openapi/AktivitetsplanV1.yaml")
//    packageName.set("no.nav.veilarbaktivitet.client")
//    apiPackage.set("no.nav.veilarbaktivitet.api")
//    modelPackage.set("no.nav.veilarbaktivitet.model")
//
//    outputDir.set("$buildDir/generated")
//}

task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateAktivitetsplanServer") {
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

tasks.named( "compileKotlin") {
    dependsOn( "generateAktivitetsplanServer")
}
java.sourceSets["main"].java.srcDir("$buildDir/generated/src/main/java")



dependencies {
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    api("javax.validation:validation-api:2.0.1.Final")
    implementation("org.realityforge.javax.annotation:javax.annotation:1.0.1")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    // avhengigheter i generert kode
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.gsonfire:gson-fire:1.8.5")
    api("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation(group= "org.threeten", name= "threetenbp", version= "1.5.1")
    implementation("io.swagger:swagger-annotations:1.6.4")
    implementation("com.squareup.okio:okio:3.0.0")
    // avhengigheter i generert kode SLUTT

}
