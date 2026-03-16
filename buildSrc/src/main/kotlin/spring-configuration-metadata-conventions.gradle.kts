import dev.freya02.botcommands.tasks.GenerateSpringConfigurationMetadataTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-conventions")
}

val compileKotlin by tasks.getting(KotlinCompile::class)

val springConfigurationMetadataSourceRoot: Provider<Directory> = layout.buildDirectory.dir("generated/spring-configuration-metadata/main/resources")
val generateSpringConfigurationMetadata by tasks.registering(GenerateSpringConfigurationMetadataTask::class) {
    classesRoot = layout.buildDirectory.dir("classes/kotlin/main").get().asFile.path

    // The task should only run for API changes
    apiClasses.from(compileKotlin.outputs.files.asFileTree.matching {
        include("**/*.class")
        exclude("**/internal/**")
    })

    outputFile = springConfigurationMetadataSourceRoot.get().file("META-INF/spring-configuration-metadata.json")
}

// Generate metadata (a resource) before processing resources
tasks.named("processResources") {
    dependsOn(generateSpringConfigurationMetadata)
}

// Register our generated sources
sourceSets {
    main {
        resources {
            srcDir(springConfigurationMetadataSourceRoot)
        }
    }
}
