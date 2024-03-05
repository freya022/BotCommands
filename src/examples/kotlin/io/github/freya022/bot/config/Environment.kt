package io.github.freya022.bot.config

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

object Environment {
    /**
     * The folder where the data and configuration directories reside.
     */
    val folder: Path = Path("test-files", "examples")

    /**
     * The mode is determined by checking if the
     * `dev-config` directory exists in the current directory.
     */
    val isDev: Boolean = folder.resolve("dev-config").exists()

    val configFolder: Path =
        folder.resolve(if (isDev) "dev-config" else "config")
    val logbackConfigPath: Path = configFolder.resolve(if (isDev) "logback-test.xml" else "logback.xml")
}