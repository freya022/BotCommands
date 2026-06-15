package dev.freya02.botcommands.tasks

import dev.freya02.bc.classlist.generator.ClassListGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.io.File

abstract class GenerateClassListTask : DefaultTask() {

    // Only for Gradle caching purposes
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classes: ConfigurableFileCollection

    @get:Input
    abstract val buildDirs: ListProperty<String>

    @get:Input
    abstract val classpath: ListProperty<String>

    @get:OutputDirectory
    abstract val outputRoot: DirectoryProperty

    @TaskAction
    fun generate() {
        val classList = ClassListGenerator.generate(buildDirs.get().map(::File), classpath.get().map(::File))
        if (classList.isBlank()) {
            return
        }

        val outputFile = outputRoot.get().asFile.resolve("META-INF").resolve("bc.classes")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(classList)
    }
}
