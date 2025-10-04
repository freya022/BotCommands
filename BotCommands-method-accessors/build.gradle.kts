import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")

    alias(libs.plugins.jmh)
}

dependencies {
    implementation(projects.botCommandsMethodAccessors.classfile)
    implementation(projects.botCommandsMethodAccessors.kotlinReflect)

    testImplementation(libs.bundles.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jmh {
    // See https://github.com/melix/jmh-gradle-plugin?tab=readme-ov-file#configuration-options
    failOnError = true // Should JMH fail immediately if any benchmark had experienced the unrecoverable error?
    humanOutputFile = project.file("reports/jmh/human.txt") // human-readable output file
    resultsFile = project.file("reports/jmh/results.txt") // results file
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
    }
}
