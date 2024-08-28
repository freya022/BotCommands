package io.github.freya022.botcommands.internal.commands.application.cache.factory

import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.NullApplicationCommandsCache
import net.dv8tion.jda.api.entities.Guild

internal data object NullApplicationCommandsCacheFactory : ApplicationCommandsCacheFactory {
    override val cacheConfig: ApplicationCommandsCacheConfig = object : ApplicationCommandsCacheConfig {
        override val checkOnline = false
        override val diffEngine = DiffEngine.NEW
        override val logDataIf = ApplicationCommandsCacheConfig.LogDataIf.NEVER
    }

    override fun create(guild: Guild?): ApplicationCommandsCache = NullApplicationCommandsCache
}