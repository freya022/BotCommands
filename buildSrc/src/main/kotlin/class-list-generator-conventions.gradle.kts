import dev.freya02.botcommands.tasks.GenerateClassListTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

val compileJava = tasks.getByName<JavaCompile>("compileJava")
val compileKotlin = tasks.getByName<KotlinCompile>("compileKotlin")

val generateClassList = tasks.register<GenerateClassListTask>("generateClassList") {
    group = "build"
    description = "Generates the ClassGraph-scanned class list from the main sources"

    buildDirs = listOf(
        layout.buildDirectory.dir("classes/java/main").get().asFile.path,
        layout.buildDirectory.dir("classes/kotlin/main").get().asFile.path,
    )
    // This is necessary as meta-annotations from dependencies cannot be resolved without them on the classpath
    classpath = configurations.compileClasspath.get().files.map { it.path }

    // Only regenerate list if classes changes
    classes.from(compileJava.outputs.files, compileKotlin.outputs.files)

    outputRoot = layout.buildDirectory.dir("generated/sources/lib-class-list/main/resources")
}

// Register our generated sources
sourceSets {
    main {
        resources {
            srcDir(generateClassList)
        }
    }
}
