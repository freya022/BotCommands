package io.github.freya022.botcommands.internal.commands.application.cache.factory

import io.github.freya022.botcommands.api.core.config.application.cache.FileApplicationCommandsCacheConfig
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.FileApplicationCommandsCache
import net.dv8tion.jda.api.entities.Guild
import kotlin.io.path.createDirectories

internal class FileApplicationCommandsCacheFactory(
    override val cacheConfig: FileApplicationCommandsCacheConfig,
    applicationId: Long
) : ApplicationCommandsCacheFactory {

    private val cachePath = cacheConfig.path.resolve("ApplicationCommands-$applicationId")

    init {
        cachePath.createDirectories()
    }

    override fun create(guild: Guild?): ApplicationCommandsCache {
        return FileApplicationCommandsCache(cachePath, guild)
    }
}