package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.core.*
import io.github.freya022.botcommands.api.core.BContext.Status
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.BStatusChangeEvent
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.service.lazy
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandsContextImpl
import io.github.freya022.botcommands.internal.commands.text.TextCommandsContextImpl
import io.github.freya022.botcommands.internal.utils.unwrap
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.loggerOf<BContext>()

@BService
internal class BContextImpl internal constructor(
    override val config: BConfig,
    override val serviceContainer: ServiceContainer,
    override val botOwners: BotOwners
) : BContext {
    override val eventDispatcher: EventDispatcher by serviceContainer.lazy()

    override var status: Status = Status.PRE_LOAD
        private set

    override val settingsProvider: SettingsProvider? by lazy { serviceContainer.getServiceOrNull() }
    override val globalExceptionHandler: GlobalExceptionHandler? by lazy { serviceContainer.getServiceOrNull() }

    override val textCommandsContext: TextCommandsContextImpl by serviceContainer.lazy()

    override val applicationCommandsContext: ApplicationCommandsContextImpl by serviceContainer.lazy()

    private val bcRegex = Regex("at ${Regex.escape("io.github.freya022.botcommands.")}(?:api|internal)[.a-z]*\\.(.+)")
    private var nextExceptionDispatch: Long = 0

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

                        channel.sendMessage(content).queue(
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
                .filterNot { "java.base/java.util.concurrent.Executors\$RunnableAdapter.call" in it }
                .filterNot { "java.base/java.util.concurrent.FutureTask.run" in it }
                .filterNot { "java.base/java.util.concurrent.ScheduledThreadPoolExecutor\$ScheduledFutureTask.run" in it }
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
        val oldStatus = this.status
        this.status = newStatus
        eventDispatcher.dispatchEvent(BStatusChangeEvent(this@BContextImpl, oldStatus, newStatus))
    }
}
