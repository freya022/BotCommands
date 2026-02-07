package dev.freya02.botcommands.jda.keepalive.internal

import net.dv8tion.jda.api.JDA

internal object JDACache {

    private val cache: MutableMap<String, Data> = hashMapOf()

    internal operator fun set(key: String, data: Data) {
        cache[key] = data
    }

    internal fun remove(key: String): Data? = cache.remove(key)

    internal class Data internal constructor(
        val configuration: JDABuilderConfiguration,
        val jda: JDA,
        val doShutdown: Runnable,
        val scheduleShutdownSignal: JDABuilderSession.ScheduleShutdownSignalWrapper,
    )
}
