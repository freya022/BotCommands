import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.setMainJvmTarget

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
}

dependencies {
    api(projects.botCommandsMethodAccessors.core)
    implementation(projects.botCommandsCore)
}

setMainJvmTarget(target = 24)

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-method-accessors-classfile",
        description = "Provides support to call methods reflectively, using generated accessors.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-method-accessors/classfile",
    )
}
