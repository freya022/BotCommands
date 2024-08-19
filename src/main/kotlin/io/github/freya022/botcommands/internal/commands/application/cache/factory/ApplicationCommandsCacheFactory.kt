package io.github.freya022.botcommands.internal.commands.application.cache.factory

import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import net.dv8tion.jda.api.entities.Guild

internal interface ApplicationCommandsCacheFactory {
    fun create(guild: Guild?): ApplicationCommandsCache
}