package io.github.freya022.wiki.config

import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.notExists

object Data {
    /**
     * Where your bot can write data if needed
     */
    val folder: Path = Environment.folder.resolve(if (Environment.isDev) "dev-data" else "data")

    /**
     * Checks whether the path exists, throwing if not.
     */
    private fun Path.validatedPath(desc: String): Path = this.also {
        if (it.notExists())
            throw FileNotFoundException("$desc at ${it.absolutePathString()} does not exist.")
    }
}