package io.github.freya022.botcommands.internal.commands.application.cache.factory

import gnu.trove.map.hash.TLongObjectHashMap
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.MemoryApplicationCommandsCache
import net.dv8tion.jda.api.entities.Guild
import okio.withLock
import java.util.concurrent.locks.ReentrantLock

internal class MemoryApplicationCommandsCacheFactory(
    override val cacheConfig: ApplicationCommandsCacheConfig
) : ApplicationCommandsCacheFactory {
    private val lock = ReentrantLock()
    private val cache = TLongObjectHashMap<ApplicationCommandsCache>()

    override fun create(guild: Guild?): ApplicationCommandsCache {
        val cacheKey = guild?.idLong ?: 0
        val commandsCache = lock.withLock {
            cache[cacheKey] ?: run {
                val commandsCache = MemoryApplicationCommandsCache(guild)
                cache.put(cacheKey, commandsCache)
                commandsCache
            }
        }

        return commandsCache
    }
}