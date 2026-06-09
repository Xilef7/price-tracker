plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("buildsrc.convention.aws-lambda")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.kotlinxSerialization)
    implementation(libs.kotlinxCoroutines)
    implementation(project(":dynamodb-service"))
    implementation(project(":shared"))
}

tasks.shadowJar {
    manifest.attributes["Main-Class"] = "com.xilef7.hourlyputter.HandlerKt"
}
