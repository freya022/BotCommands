import dev.freya02.botcommands.plugins.PublishedProjectEnvironmentConfig

plugins {
    id("org.jetbrains.dokka")
}

val publishedEnvironment = project.extensions.getByType<PublishedProjectEnvironmentConfig>()

val effectiveTag = publishedEnvironment.effectiveTag

dokka {
    dokkaSourceSets.configureEach {
        jdkVersion = 17

        perPackageOption {
            matchingRegex = ".*internal.*"
            suppress = true
        }

        sourceLink {
            localDirectory = rootDir
            remoteUrl("https://github.com/freya022/BotCommands/tree/${effectiveTag}")
            remoteLineSuffix = "#L"
        }

        // TODO set them on projects which expose such dependencies, add util functions for each
        //  also use version catalog
        externalDocumentationLinks.register("JDA") {
            url("https://docs.jda.wiki")
            packageListUrl("https://docs.jda.wiki/element-list")
        }

        externalDocumentationLinks.register("JetBrainsAnnotations") {
            url("https://javadoc.io/doc/org.jetbrains/annotations/26.0.2")
            packageListUrl("https://javadoc.io/doc/org.jetbrains/annotations/26.0.2/package-list")
        }

        externalDocumentationLinks.register("Spring") {
            url("https://docs.spring.io/spring-framework/docs/current/javadoc-api")
            packageListUrl("https://docs.spring.io/spring-framework/docs/current/javadoc-api/element-list")
        }

        externalDocumentationLinks.register("Bucket4J") {
            url("https://javadoc.io/doc/com.bucket4j/bucket4j_jdk17-core/8.14.0")
            packageListUrl("https://javadoc.io/doc/com.bucket4j/bucket4j_jdk17-core/8.14.0/element-list")
        }
    }
}

afterEvaluate {
    val conf = rootProject.configurations["dokka"]

    if (conf.dependencies.none { it is ProjectDependency && it.path == this.path }) {
        error("$this must be added to the Dokka aggregate dependencies in the root project")
    }
}
