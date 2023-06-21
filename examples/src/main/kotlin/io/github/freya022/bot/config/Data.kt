package io.github.freya022.bot.config

import java.nio.file.Path

object Data {
    /**
     * Where your bot can write data if needed
     */
    val folder: Path = Environment.folder.resolve(if (Environment.isDev) "dev-data" else "data")
}