import dev.freya02.botcommands.tasks.GenerateClassListTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

val compileJava by tasks.getting(JavaCompile::class)
val compileKotlin by tasks.getting(KotlinCompile::class)

val generateClassList by tasks.registering(GenerateClassListTask::class) {
    buildDirs.from(
        layout.buildDirectory.dir("classes/java/main"),
        layout.buildDirectory.dir("classes/kotlin/main"),
    )
    // This is necessary as meta-annotations from dependencies cannot be resolved without them on the classpath
    classpath.from(configurations.compileClasspath)

    // Only regenerate list if classes changes
    classes.from(compileJava.outputs, compileKotlin.outputs)

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
