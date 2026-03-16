plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinx.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization)
    implementation(libs.classgraph)
}
