plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(awssdk.services.dynamodb)
    implementation(awssdk.runtime.smithy.kotlin.http.client.engine.crt)
    implementation(project(":shared"))
}
