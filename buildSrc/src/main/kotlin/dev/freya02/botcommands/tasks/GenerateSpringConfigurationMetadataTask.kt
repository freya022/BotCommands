package dev.freya02.botcommands.tasks

import dev.freya02.botcommands.spring.metadata.generator.SpringConfigurationMetadataGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class GenerateSpringConfigurationMetadataTask : DefaultTask() {
    // This isn't used by the generator as ClassGraph will discover the classes by itself,
    // this is only to cache the task result
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apiClasses: ConfigurableFileCollection

    @get:Input
    abstract val classesRoot: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val json = SpringConfigurationMetadataGenerator.generate(File(classesRoot.get()))

        val outputFile = outputFile.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(json)
    }
}
