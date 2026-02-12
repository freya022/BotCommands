import dev.freya02.botcommands.plugins.PublishedProjectEnvironmentConfig
import dev.freya02.botcommands.utils.registerJDADocs

plugins {
    id("org.jetbrains.dokka")
}

val publishedEnvironment = project.extensions.getByType<PublishedProjectEnvironmentConfig>()

val effectiveTag = publishedEnvironment.effectiveTag

dokka {
    dokkaSourceSets.configureEach {
        jdkVersion = 25

        perPackageOption {
            matchingRegex = ".*internal.*"
            suppress = true
        }

        sourceLink {
            localDirectory = rootDir
            remoteUrl("https://github.com/freya022/BotCommands/tree/${effectiveTag}")
            remoteLineSuffix = "#L"
        }

        registerJDADocs()
    }
}

afterEvaluate {
    val conf = rootProject.configurations["dokka"]

    if (conf.dependencies.none { it is ProjectDependency && it.path == this.path }) {
        error("$this must be added to the Dokka aggregate dependencies in the root project")
    }
}
