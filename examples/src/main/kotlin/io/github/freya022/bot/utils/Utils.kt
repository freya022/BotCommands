package io.github.freya022.bot.utils

import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

fun namedDefaultScope(name: String, poolSize: Int): CoroutineScope {
    val lock = ReentrantLock()
    var count = 0
    val executor = Executors.newScheduledThreadPool(poolSize) {
        Thread(it).apply {
            lock.withLock {
                this.name = "$name ${++count}"
            }
        }
    }

    return getDefaultScope(pool = executor, context = CoroutineName(name))
}