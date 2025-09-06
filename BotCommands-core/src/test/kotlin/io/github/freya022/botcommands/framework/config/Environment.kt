package io.github.freya022.botcommands.framework.config

import java.nio.file.Path
import kotlin.io.path.Path

object Environment {
    /**
     * The folder where the data and configuration directories reside.
     */
    val folder: Path = Path("test-files", "test")

    val configFolder: Path = folder.resolve("dev-config")
    val logbackConfigPath: Path = configFolder.resolve("logback-test.xml")
}
