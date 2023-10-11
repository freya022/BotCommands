package io.github.freya022.botcommands.api.core.utils

import dev.minn.jda.ktx.events.getDefaultScope
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.toKLogger
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("UnusedReceiverParameter")
inline fun <reified T : Any> KotlinLogging.logger(): KLogger =
    LoggerFactory.getLogger(T::class.java).toKLogger()

private val stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

/**
 * Reads a resource relative from the calling class.
 *
 * If the URL starts with a `/`, then the resource will be read from the root,
 * so either the JAR root, or from your `resources` directory.
 *
 * If the URL does not start with a `/`, then it is read relative to the package of the calling class.
 */
fun readResource(url: String): InputStream {
    val callerClass = stackWalker.callerClass
    return requireNotNull(callerClass.getResourceAsStream(url)) {
        "Resource of class " + callerClass.simpleName + " at URL '" + url + "' does not exist"
    }
}

/**
 * Reads a resource relative as a string from the calling class.
 *
 * If the URL starts with a `/`, then the resource will be read from the root,
 * so either the JAR root, or from your `resources` directory.
 *
 * If the URL does not start with a `/`, then it is read relative to the package of the calling class.
 */
fun readResourceAsString(url: String): String {
    val callerClass = stackWalker.callerClass
    val stream = requireNotNull(callerClass.getResourceAsStream(url)) {
        "Resource of class " + callerClass.simpleName + " at URL '" + url + "' does not exist"
    }
    return stream.bufferedReader().use { it.readText() }
}

/**
 * Reads a resource relative from the calling class.
 *
 * If the URL starts with a `/`, then the resource will be read from the root,
 * so either the JAR root, or from your `resources` directory.
 *
 * If the URL does not start with a `/`, then it is read relative to the package of the calling class.
 */
fun <R> withResource(url: String, block: (InputStream) -> R): R {
    return readResource(url).use(block)
}

/**
 * Creates a [CoroutineScope] with incremental thread naming, uses [getDefaultScope] under the hood.
 *
 * @param name         The base name of the threads and coroutines, will be prefixed by the number if [corePoolSize] > 1
 * @param corePoolSize The number of threads to keep in the pool, even if they are idle
 * @param job          The parent job used for coroutines which can be used to cancel all children, uses [SupervisorJob] by default
 * @param errorHandler The [CoroutineExceptionHandler] used for handling uncaught exceptions,
 *                     uses a logging handler which cancels the parent job on [Error] by default
 * @param context      Any additional context to add to the scope, uses [EmptyCoroutineContext] by default
 */
fun namedDefaultScope(
    name: String,
    corePoolSize: Int,
    job: Job? = null,
    errorHandler: CoroutineExceptionHandler? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineScope {
    require(corePoolSize >= 0) {
        "Pool size must be positive"
    }

    val lock = ReentrantLock()
    var count = 0
    val executor = Executors.newScheduledThreadPool(corePoolSize) {
        Thread(it).apply {
            if (corePoolSize <= 1) {
                this.name = name
            } else {
                lock.withLock {
                    this.name = "$name ${++count}"
                }
            }
        }
    }

    return getDefaultScope(pool = executor, context = CoroutineName(name) + context, job = job, errorHandler = errorHandler)
}