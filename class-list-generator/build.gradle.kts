plugins {
    alias(libs.plugins.kotlin)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.classgraph)
    implementation("dev.freya02:reflection-metadata-commons")
}
