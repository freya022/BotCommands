plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.compilerArgs.add("-parameters")
}

// Main source sets default to minimum Java version - 17
// Modules that use higher versions will override, while tests and other source sets uses the toolchain's version
tasks.named<JavaCompile>("compileJava") {
    options.release = 17
}
