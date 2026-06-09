plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(libs.kotlinxCoroutines)
    implementation(platform("org.http4k:http4k-bom:6.53.0.0"))
    implementation("org.http4k:http4k-connect-amazon-dynamodb")
    implementation(project(":shared"))
}
