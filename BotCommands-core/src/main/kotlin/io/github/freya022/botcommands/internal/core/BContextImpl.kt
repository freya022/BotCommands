package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BContext.Status
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.GlobalExceptionHandler
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.events.BShutdownEvent
import io.github.freya022.botcommands.api.core.events.BStatusChangeEvent
import io.github.freya022.botcommands.api.core.hooks.EventDispatcher
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.service.lazy
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.utils.takeIfFinite
import io.github.freya022.botcommands.internal.utils.unwrap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.loggerOf<BContext>()

@BService
class BContextImpl internal constructor(
    override val config: BConfig,
    override val serviceContainer: ServiceContainer,
    override val botOwners: BotOwners
) : BContext {
    override val eventDispatcher: EventDispatcher by serviceContainer.lazy()

    private val _status: AtomicReference<Status> = AtomicReference(Status.PRE_LOAD)
    override val status: Status get() = _status.get()

    private val shutdownLock = ReentrantLock()
    private var forceShutdown = false

    override val globalExceptionHandler: GlobalExceptionHandler? by lazy { serviceContainer.getServiceOrNull() }

    override val applicationCommandsContext: ApplicationCommandsContext by serviceContainer.lazy()

    private val bcRegex = Regex("at ${Regex.escape("io.github.freya022.botcommands.")}(?:api|internal)[.a-z]*\\.(.+)")
    private var nextExceptionDispatch: Long = 0

    private val statusLock: ReentrantLock = ReentrantLock()
    private val statusCondition: Condition = statusLock.newCondition()

    override fun dispatchException(message: String, t: Throwable?, extraContext: Map<String, Any?>) {
        if (config.disableExceptionsInDMs) return //Don't send DM exceptions in dev mode

        if (nextExceptionDispatch < System.currentTimeMillis()) {
            nextExceptionDispatch = System.currentTimeMillis() + 10.minutes.inWholeMilliseconds

            val content = getExceptionContent(message, t, extraContext)
            botOwners.ownerIds.forEach { ownerId ->
                ownerId
                    .let(jda::openPrivateChannelById)
                    .queue { channel ->
                        fun onUserNotFound() {
                            if (botOwners.ownerIds.size > 1)
                                logger.warn { "Could not send exception DM to team member '${channel.user?.effectiveName}' (${ownerId})" }
                            else
                                logger.warn { "Could not send exception DM to owner '${channel.user?.effectiveName}' ($ownerId)" }
                        }

                        channel.sendMessage(content).useComponentsV2(false).queue(
                            null,
                            ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {
                                onUserNotFound()
                            }
                        )
                    }
            }
        }
    }

    override fun getExceptionContent(message: String, t: Throwable?, extraContext: Map<String, Any?>): String = buildString {
        appendLine(message)
        if (extraContext.isNotEmpty()) {
            appendLine("## Context")
            appendLine(extraContext.entries.joinToString("\n") { (name, value) -> "**$name:** $value" })
        }
        if (t != null) {
            append("## Filtered exception\n```\n")
            val stackTraceLines = t.unwrap().stackTraceToString()
                .lineSequence()
                .filterNot { "jdk.internal" in it }
                .filterNot { "java.base/java.lang.reflect.Method" in it }
                .filterNot { "kotlin.reflect.full" in it }
                .filterNot { "kotlin.reflect.jvm.internal" in it }
                .filterNot { "kotlin.coroutines.jvm.internal" in it }
                .filterNot { "dev.reformator.stacktracedecoroutinator" in it }
                .filterNot { "kotlinx.coroutines.DispatchedTask.run" in it }
                .filterNot { $$"java.base/java.util.concurrent.Executors$RunnableAdapter.call" in it }
                .filterNot { "java.base/java.util.concurrent.FutureTask.run" in it }
                .filterNot { $$"java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run" in it }
                .filterNot { "java.base/java.util.concurrent.ThreadPoolExecutor" in it }
                .filterNot { "java.base/java.lang.Thread.run" in it }
                //Remove lines without a source line number,
                // they are usually generated methods like "invokeSuspend"
                .filterNot { it.endsWith(".kt)") }
                .filterNot { ".access$" in it }
                .map { it.replace("    ", "\t") }
                .map {
                    bcRegex.replace(it) { matchResult ->
                        val remaining = matchResult.groupValues[1]
                        "at BC-${BCInfo.VERSION}/$remaining"
                    }
                }

            for (stackTraceLine in stackTraceLines) {
                if (this.length + stackTraceLine.length + 3 + 1 + 63 > Message.MAX_CONTENT_LENGTH) break
                appendLine(stackTraceLine)
            }
            // Replace last newline with the code block end
            replace(lastIndex, lastIndex + 1, "```")
        }
        append("\nPlease check the logs for more detail and possible exceptions")
    }

    internal suspend fun setStatus(newStatus: Status) {
        val oldStatus = statusLock.withLock {
            val oldStatus = _status.getAndSet(newStatus)
            statusCondition.signalAll()
            oldStatus
        }
        if (oldStatus != newStatus)
            eventDispatcher.dispatchEvent(BStatusChangeEvent(this, oldStatus, newStatus))
    }

    override fun shutdown(): Unit = shutdownLock.withLock {
        if (status == Status.SHUTTING_DOWN || status == Status.SHUTDOWN) return
        val jda = getServiceOrNull<JDA>()
        doShutdown(jda)
    }

    override fun shutdownNow(): Unit = shutdownLock.withLock {
        if (status == Status.SHUTDOWN) return
        forceShutdown = true
        val jda = getServiceOrNull<JDA>()
        if (status != Status.SHUTTING_DOWN && status != Status.SHUTDOWN) {
            doShutdown(jda)
        }

        if (jda != null) {
            shutdownJDANow(jda)
            // 'shutdownCoroutineScopes' is already scheduled by 'doShutdown'
        } else {
            shutdownCoroutineScopes()
        }
    }

    private fun doShutdown(jda: JDA?) {
        runBlocking { setStatus(Status.SHUTTING_DOWN) }

        if (jda != null) {
            scheduleShutdownSignal(jda)

            shutdownJDA(jda)
        } else {
            shutdownCoroutineScopes()
        }
    }

    /**
     * Asynchronously listens for all shards to be shut down,
     * then fires a [BShutdownEvent] and shuts down all coroutine scopes.
     */
    private fun scheduleShutdownSignal(jda: JDA) {
        fun signalShutdown() = runBlocking {
            statusLock.withLock {
                if (status == Status.SHUTDOWN)
                    return@runBlocking shutdownCoroutineScopes()
            }
            setStatus(Status.SHUTDOWN)
            eventDispatcher.dispatchEvent(BShutdownEvent(this@BContextImpl))
            // Shutdown the pools *after* dispatching
            shutdownCoroutineScopes()
        }

        val shards = jda.shardManager?.shards ?: listOf(jda)
        val countdown = AtomicInteger(shards.size)
        shards.forEach {
            it.listenOnce(ShutdownEvent::class.java).subscribe {
                if (countdown.decrementAndGet() == 0) {
                    signalShutdown()
                }
            }
        }
    }

    override fun awaitShutdown(timeout: Duration): Boolean {
        val deadline = Clock.System.now() + (timeout.takeIfFinite() ?: Duration.INFINITE)
        fun durationUntilDeadline(): Duration = deadline - Clock.System.now()

        if (!awaitJDAShutdown(::durationUntilDeadline))
            return false

        statusLock.withLock {
            while (status != Status.SHUTDOWN) {
                if (!statusCondition.await(durationUntilDeadline().inWholeMilliseconds, TimeUnit.MILLISECONDS)) {
                    return false
                }
            }
        }

        return true
    }

    private fun awaitJDAShutdown(durationUntilDeadlineFn: () -> Duration): Boolean {
        val jda = getServiceOrNull<JDA>()
        if (jda == null) {
            logger.debug { "Not awaiting JDA shutdown as there is no JDA instance registered" }
            return true
        }
        val shardManager = jda.shardManager
        if (shardManager != null) {
            shardManager.shards.forEach { shard ->
                if (!shard.awaitShutdown(durationUntilDeadlineFn().toJavaDuration())) {
                    return false
                }
            }
        } else {
            if (!jda.awaitShutdown(durationUntilDeadlineFn().toJavaDuration())) {
                return false
            }
        }

        return true
    }

    private fun shutdownJDA(jda: JDA) {
        val shardManager = jda.shardManager
        if (shardManager != null) {
            shardManager.shutdown()
        } else {
            jda.shutdown()
        }
    }

    private fun shutdownJDANow(jda: JDA) {
        val shardManager = jda.shardManager
        if (shardManager != null) {
            shardManager.shutdown()

            shardManager.shardCache.forEach { jda ->
                // The shard manager may not be configured to shut down immediately,
                // so we try to force it here
                jda.shutdownNow()
            }
        } else {
            jda.shutdownNow()
        }
    }

    private fun shutdownCoroutineScopes() {
        BCoroutineScopesConfig::class
            .memberProperties
            .asSequence()
            .filter { it.returnType.jvmErasure == CoroutineScope::class }
            .map { it.get(coroutineScopesConfig) }
            .map { it as CoroutineScope }
            .forEach {
                it.shutdownExecutor()
            }
    }

    private fun CoroutineScope.shutdownExecutor() {
        val executor = coroutineContext[ExecutorCoroutineDispatcher]?.executor as? ExecutorService
        if (forceShutdown) {
            cancel("Cancelled by shutdown")
            executor?.shutdownNow()
        } else {
            executor?.shutdown()
        }
    }
}
