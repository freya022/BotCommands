import org.gradle.api.DefaultTask
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.time.Instant
import javax.inject.Inject

@DisableCachingByDefault
abstract class GenerateBCInfoTask : DefaultTask() {

    @get:InputFile
    val inputFile = project.layout.projectDirectory.file($$"src/main/java/io/github/freya022/botcommands/api/$BCInfo.java")

    @get:OutputDirectory
    val outputDir = project.layout.buildDirectory.dir("generated/sources/BotCommands/main/java")

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
        val attributes = mapOf(
            "version-major" to version.major,
            "version-minor" to version.minor,
            "version-revision" to version.revision,
            "version-classifier" to (version.classifier ?: "null"),
            "branch-name" to (GitUtils.getCommitBranch(logger, providers, projectDir) ?: "null"),
            "commit-hash" to (GitUtils.getCommitHash(logger, providers, projectDir)?.take(10) ?: "null"),
            "build-jda-version" to jdaVersion,
            "build-time" to Instant.now().toEpochMilli().toString(),
        )

        val initialContent = inputFile.asFile.readText()
        val filteredContent = initialContent.replaceTokens(attributes).replace($$"$BCInfo", "BCInfo")

        val bcInfoFile = outputDir.get().file("io/github/freya022/botcommands/api/BCInfo.java").asFile
        bcInfoFile.parentFile.mkdirs()
        bcInfoFile.writeText(filteredContent)
    }

    private fun String.replaceTokens(map: Map<String, String>): String {
        return map.entries.fold(this) { current, (key, value) -> current.replace("@$key@", value) }
    }
}
