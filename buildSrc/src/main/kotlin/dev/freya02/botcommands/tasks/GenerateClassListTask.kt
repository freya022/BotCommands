package dev.freya02.botcommands.tasks

import dev.freya02.bc.classlist.generator.ClassListGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class GenerateClassListTask : DefaultTask() {

    // For Gradle caching purposes and implicit dependency on compile tasks
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classes: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val buildDirs: ConfigurableFileCollection

    @get:Classpath
    abstract val classpath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputRoot: DirectoryProperty

    @TaskAction
    fun generate() {
        val classList = ClassListGenerator.generate(buildDirs.files, classpath.files)
        if (classList.isBlank()) {
            return
        }

        val outputFile = outputRoot.get().asFile.resolve("META-INF").resolve("bc.classes")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(classList)
    }
}
