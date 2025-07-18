package io.github.freya022.botcommands.test.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.core.utils.awaitCatching
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.internal.requests.Requester
import org.slf4j.LoggerFactory

private val mutex = Mutex()

suspend fun <T> RestAction<T>.awaitAndLog(): T {
    val logger = LoggerFactory.getLogger(Requester::class.java.name) as Logger
    return mutex.withLock {
        val oldLevel = logger.level
        try {
            logger.level = Level.TRACE
            await()
        } finally {
            logger.level = oldLevel
        }
    }
}

suspend fun RestAction<*>.awaitAndLogCatching() {
    val logger = LoggerFactory.getLogger(Requester::class.java.name) as Logger
    val oldLevel = logger.level
    mutex.withLock {
        try {
            logger.level = Level.TRACE
            awaitCatching().onFailure { it.printStackTrace() }
        } finally {
            logger.level = oldLevel
        }
    }
}
