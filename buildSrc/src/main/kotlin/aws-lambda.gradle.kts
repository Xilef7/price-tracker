// The code in this file is a convention plugin - a Gradle mechanism for sharing reusable build logic.
// `buildSrc` is a Gradle-recognized directory and every plugin there will be easily available in the rest of the build.
package buildsrc.convention

import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    id("com.gradleup.shadow")
    kotlin("jvm")
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    val http4kVersion = "6.53.0.0"

    implementation("org.http4k:http4k-serverless-lambda:$http4kVersion")
    implementation("org.http4k:http4k-serverless-lambda-runtime:$http4kVersion")

    val log4jVersion = "2.25.4"
    runtimeOnly("org.apache.logging.log4j:log4j-api:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-layout-template-json:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:1.6.4")
}

tasks.shadowJar {
    filesMatching("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    transform<Log4j2PluginsCacheFileTransformer>()
}

tasks.register<Zip>("buildZip") {
    group = "build"
    description = "Create a combined zip of project and runtime dependencies"

    from(tasks.compileKotlin)
    from(tasks.processResources)
    into("lib") {
        from(configurations.runtimeClasspath)
    }
}
