plugins {
    id("kotlin-conventions")

    alias(libs.plugins.jmh)
}

dependencies {
    testImplementation(libs.bundles.test)

    testImplementation(projects.botCommandsMethodAccessors.classfile)
    testImplementation(projects.botCommandsMethodAccessors.kotlinReflect)

    jmh(projects.botCommandsMethodAccessors.classfile)
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
