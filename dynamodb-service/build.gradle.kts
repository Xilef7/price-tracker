plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(awssdk.services.dynamodb)
    implementation(project(":shared"))
}
