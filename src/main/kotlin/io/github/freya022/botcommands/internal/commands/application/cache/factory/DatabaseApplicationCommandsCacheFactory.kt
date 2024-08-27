package io.github.freya022.botcommands.internal.commands.application.cache.factory

import gnu.trove.map.hash.TLongObjectHashMap
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.DatabaseApplicationCommandsCache
import io.github.freya022.botcommands.internal.core.db.InternalDatabase
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class DatabaseApplicationCommandsCacheFactory(
    override val cacheConfig: ApplicationCommandsCacheConfig,
    private val database: InternalDatabase,
    private val applicationId: Long
) : ApplicationCommandsCacheFactory {
    private val lock = ReentrantLock()
    private val cache = TLongObjectHashMap<ApplicationCommandsCache>()

    override fun create(guild: Guild?): ApplicationCommandsCache {
        val cacheKey = guild?.idLong ?: 0
        val commandsCache = lock.withLock {
            cache[cacheKey] ?: run {
                val commandsCache = DatabaseApplicationCommandsCache(guild, database, applicationId)
                cache.put(cacheKey, commandsCache)
                commandsCache
            }
        }

        return commandsCache
    }
}