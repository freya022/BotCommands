package io.github.freya022.bot.config

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.toPath

object Environment {
    /**
     * The mode is determined by checking if the
     * `dev-config` directory exists in the current directory.
     */
    val isDev: Boolean = Path("dev-config").exists()

    /**
     * The folder where the data and configuration directories reside.
     */
    val folder: Path = when {
        isDev -> Path("")
        else -> {
            val jarPath = javaClass.protectionDomain.codeSource.location.toURI().toPath()
            if (jarPath.extension != "jar") {
                throw IllegalStateException("Production environment detected (no 'dev-config' folder), but file at $jarPath isn't a JAR")
            }

            jarPath.parent
        }
    }

    val configFolder: Path =
        folder.resolve(if (isDev) "dev-config" else "config")
    val logbackConfigPath: Path = configFolder.resolve(if (isDev) "logback-test.xml" else "logback.xml")
}