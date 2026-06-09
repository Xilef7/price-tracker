// The code in this file is a convention plugin - a Gradle mechanism for sharing reusable build logic.
// `buildSrc` is a Gradle-recognized directory and every plugin there will be easily available in the rest of the build.
package buildsrc.convention

plugins {
    id("com.gradleup.shadow")
    kotlin("jvm")
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation("com.amazonaws:aws-lambda-java-core:1.4.0")
    implementation("com.amazonaws:aws-lambda-java-events:3.16.1")

    runtimeOnly("org.slf4j:slf4j-simple:2.0.18")
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
