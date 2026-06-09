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
    implementation("com.amazonaws:aws-lambda-java-events:3.16.1")
}
