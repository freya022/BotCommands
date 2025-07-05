package io.github.freya022.botcommands.internal.commands.application.cache.factory

import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import net.dv8tion.jda.api.entities.Guild

@InterfacedService(acceptMultiple = false)
internal sealed interface ApplicationCommandsCacheFactory {
    val cacheConfig: ApplicationCommandsCacheConfig

    fun create(guild: Guild?): ApplicationCommandsCache
}