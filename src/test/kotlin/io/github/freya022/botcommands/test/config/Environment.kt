package io.github.freya022.botcommands.test.config

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

object Environment {
    /**
     * The mode is determined by checking if the
     * `dev-config` directory exists in the current directory.
     */
    val isDev: Boolean = Path("dev-config").exists()

    /**
     * The folder where the data and configuration directories reside.
     *
     * This is the current **working directory**.
     */
    val folder: Path = Path("")

    val configFolder: Path =
        folder.resolve(if (isDev) "dev-config" else "config")
    val logbackConfigPath: Path = configFolder.resolve(if (isDev) "logback-test.xml" else "logback.xml")
}