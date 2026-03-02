package dev.freya02.botcommands.restarter.internal.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.isDirectory

private val logger = KotlinLogging.logger { }

internal object AppClasspath {

    internal val paths: List<Path>

    init {
        val resources = Thread.currentThread().contextClassLoader.getResources("META-INF/BotCommands-restarter.properties")

        val excludePatterns = buildSet {
            resources.iterator().forEach { url ->
                val properties = url.openStream().use { inputStream ->
                    val prop = Properties()
                    prop.load(inputStream)
                    prop
                }

                // Load "restart.exclude.[patternName]=[pattern]"
                for ((key, value) in properties) {
                    if (key !is String) continue
                    if (value !is String) continue

                    val patternName = key.substringAfter("restart.exclude.", missingDelimiterValue = "")
                    if (patternName.isNotBlank() && value.isNotBlank()) {
                        add(value.toRegex())
                    }
                }
            }
        }

        logger.debug { "Restart classpath exclude patterns: $excludePatterns" }

        val (includedPaths, excludedPaths) = ManagementFactory.getRuntimeMXBean().classPath
            .split(File.pathSeparator)
            .map(::Path)
            .filter { it.isDirectory() }
            .partition { path ->
                val uri = path.toUri().toString()
                if (excludePatterns.any { it.containsMatchIn(uri) })
                    return@partition false // Exclude

                true // Include
            }

        logger.info { "Restart classpath includes (+ JARs) $includedPaths" }
        logger.info { "Restart classpath excludes $excludedPaths" }

        paths = includedPaths
    }
}
