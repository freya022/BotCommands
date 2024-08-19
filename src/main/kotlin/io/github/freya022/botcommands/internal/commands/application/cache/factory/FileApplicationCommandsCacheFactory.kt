package io.github.freya022.botcommands.internal.commands.application.cache.factory

import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.FileApplicationCommandsCache
import net.dv8tion.jda.api.entities.Guild
import java.nio.file.Path

internal class FileApplicationCommandsCacheFactory(private val cachePath: Path) : ApplicationCommandsCacheFactory {
    override fun create(guild: Guild?): ApplicationCommandsCache {
        return FileApplicationCommandsCache(cachePath, guild)
    }
}