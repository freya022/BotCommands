package dev.freya02.botcommands.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

fun Project.configureTests(byteBuddyAgentDep: Provider<MinimalExternalModuleDependency>) {
    val byteBuddyAgent = configurations.create("byteBuddyAgent") { isTransitive = false }

    dependencies {
        byteBuddyAgent(byteBuddyAgentDep)
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        jvmArgs("-javaagent:${byteBuddyAgent.asPath}")
        systemProperties["net.bytebuddy.safe"] = true
    }
}
