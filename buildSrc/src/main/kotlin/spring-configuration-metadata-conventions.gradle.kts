import dev.freya02.botcommands.tasks.GenerateSpringConfigurationMetadataTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-conventions")
}

val compileKotlin = tasks.getByName<KotlinCompile>("compileKotlin")

val generateSpringConfigurationMetadata = tasks.register<GenerateSpringConfigurationMetadataTask>("generateSpringConfigurationMetadata") {
    classesRoot = layout.buildDirectory.dir("classes/kotlin/main").get().asFile.path

    // The task should only run for API changes
    apiClasses.from(compileKotlin.outputs.files.asFileTree.matching {
        include("**/*.class")
        exclude("**/internal/**")
    })

    outputRoot = layout.buildDirectory.dir("generated/sources/spring-configuration-metadata/main/resources")
}

// Register our generated sources
sourceSets {
    main {
        resources {
            srcDir(generateSpringConfigurationMetadata)
        }
    }
}
