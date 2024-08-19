package io.github.freya022.botcommands.internal.commands.application.cache.factory

import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.NullApplicationCommandsCache
import net.dv8tion.jda.api.entities.Guild

internal object NullApplicationCommandsCacheFactory : ApplicationCommandsCacheFactory {
    override fun create(guild: Guild?): ApplicationCommandsCache = NullApplicationCommandsCache
}