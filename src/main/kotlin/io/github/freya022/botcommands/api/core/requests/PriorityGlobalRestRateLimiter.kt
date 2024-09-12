package io.github.freya022.botcommands.api.core.requests

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.freya022.botcommands.api.core.errorNull
import io.github.freya022.botcommands.internal.core.exceptions.getDiagnosticVersions
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.requests.RestRateLimiter
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import java.util.PriorityQueue
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger { }

/**
 * An implementation of [RestRateLimiter] which handles the global rate limit (50/s),
 * and in which low-priority requests are queued last to the [delegate] (such as application command updates).
 *
 * While JDA already reads the global rate limit headers, it does not prevent doing 50+ requests on different buckets.
 * For example, when updating commands on 50+ guilds, JDA would launch all requests in parallel,
 * as each guild has its own bucket, resulting in a hefty rate limit.
 *
 * When using this, guild commands will be updated as fast as possible, otherwise, it will be slowed down.
 *
 * **It is highly recommended using this (or your own implementation) with large bots.**
 *
 * ### Usage
 *
 * You will need to configure the RestConfig on your [DefaultShardManagerBuilder][DefaultShardManagerBuilder.setRestConfig].
 *
 * **Example:**
 * ```kt
 * val restConfig = RestConfig()
 *     .setRateLimiterFactory { rlConfig: RateLimitConfig ->
 *         PriorityGlobalRestRateLimiter(SequentialRestRateLimiter(rlConfig))
 *     }
 * setRestConfig(restConfig)
 * ```
 */
class PriorityGlobalRestRateLimiter(private val delegate: RestRateLimiter) : RestRateLimiter {
    private class PriorityWork(val task: RestRateLimiter.Work) : Comparable<PriorityWork> {
        override fun compareTo(other: PriorityWork): Int {
            return task.priority.compareTo(other.task.priority)
        }

        // Natural order
        // * Note: this can technically go in an IdentityHashMap as the routes are constants
        private val RestRateLimiter.Work.priority: Int get() = when (this.route.baseRoute) {
            // Retrieve first as they can avoid work if no update is necessary
            Route.Interactions.GET_COMMANDS -> 1
            Route.Interactions.UPDATE_COMMANDS -> 2
            // Do guild requests last
            Route.Interactions.GET_GUILD_COMMANDS -> 3
            Route.Interactions.UPDATE_GUILD_COMMANDS -> 4
            else -> 0
        }
    }

    // 50/s
    private val bucket = Bucket.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(50)
                .refillIntervally(50, 1.seconds.toJavaDuration())
                .build()
        )
        .build()
        .asScheduler()
    private val rateLimitScheduler = Executors.newSingleThreadScheduledExecutor {
        thread(name = "Global RateLimiter", start = false) {}
    }

    private val lock = ReentrantLock()
    private val queue = PriorityQueue<PriorityWork>()

    // Queue the task in a PriorityQueue, so we can give the most important tasks first to the delegate
    override fun enqueue(task: RestRateLimiter.Work): Unit = lock.withLock {
        if (isStopped) return

        queue.offer(PriorityWork(task))
        bucket.consume(1, rateLimitScheduler).thenApply {
            val priorityTask = pollWorkQueue(submittedTask = task) ?: return@thenApply
            delegate.enqueue(priorityTask)
        }
    }

    private fun pollWorkQueue(submittedTask: RestRateLimiter.Work): RestRateLimiter.Work? = lock.withLock {
        return queue.poll()?.task ?: return logger.errorNull {
            """
                Queue is empty! Task that may or may not have been processed earlier:
                Route: ${submittedTask.route}
                Is done: ${submittedTask.isDone}
                Is skipped: ${submittedTask.isSkipped}
                Is cancelled: ${submittedTask.isCancelled}
                Version: ${getDiagnosticVersions()}
            """
        }
    }

    override fun stop(shutdown: Boolean, callback: Runnable) = lock.withLock {
        if (shutdown) {
            rateLimitScheduler.shutdownNow()
        } else {
            rateLimitScheduler.shutdown()
        }
        delegate.stop(shutdown, callback)
    }

    override fun isStopped(): Boolean {
        return rateLimitScheduler.isShutdown
    }

    override fun cancelRequests(): Int = lock.withLock {
        val toBeCancelled = queue.filter { !it.task.isPriority && !it.task.isCancelled }
        queue.removeAll(toBeCancelled)
        toBeCancelled.forEach { it.task.cancel() }

        return toBeCancelled.size + delegate.cancelRequests()
    }
}