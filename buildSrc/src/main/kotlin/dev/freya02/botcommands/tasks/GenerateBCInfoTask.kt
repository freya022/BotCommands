package dev.freya02.botcommands.tasks

import dev.freya02.botcommands.utils.GitUtils
import dev.freya02.botcommands.utils.Version
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.time.Instant
import javax.inject.Inject

@DisableCachingByDefault
abstract class GenerateBCInfoTask : DefaultTask() {

    @get:OutputDirectory
    val outputDir = project.layout.buildDirectory.dir("generated/sources/BotCommands/main/resources")

    @get:Input
    val projectDir: String = project.projectDir.absolutePath

    @get:Inject
    abstract val providers: ProviderFactory

    @get:Input
    val version: Version = project.version as Version

    @get:Input
    val jdaVersion = project.configurations.getByName("compileClasspath")
        .incoming.dependencies.find { it.name.endsWith("JDA") }!!.version!!

    @TaskAction
    fun run() {
        val content = """
            BUILD_TIME = ${Instant.now().toEpochMilli()}
            VERSION_MAJOR = ${version.major}
            VERSION_MINOR = ${version.minor}
            VERSION_REVISION = ${version.revision}
            VERSION_CLASSIFIER = ${version.classifier ?: "null"}
            BRANCH_NAME = ${GitUtils.getCommitBranch(logger, providers, projectDir) ?: "null"}
            COMMIT_HASH = ${GitUtils.getCommitHash(logger, providers, projectDir)?.take(10) ?: "null"}
            BUILD_JDA_VERSION = $jdaVersion
        """.trimIndent()

        val bcInfoFile = outputDir.get().file("BCInfo.properties").asFile
        bcInfoFile.parentFile.mkdirs()
        bcInfoFile.writeText(content)
    }
}
