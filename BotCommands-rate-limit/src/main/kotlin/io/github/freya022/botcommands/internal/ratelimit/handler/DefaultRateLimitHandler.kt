package io.github.freya022.botcommands.internal.ratelimit.handler

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.ratelimit.handler.RateLimitHandler
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.util.ServiceLoader
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds

class DefaultRateLimitHandler(
    val scope: RateLimitScope,
    val deleteOnRefill: Boolean,
) : RateLimitHandler {

    private val handlers = ServiceLoader.load(RequestHandler::class.java)

    override suspend fun onRateLimit(context: RateLimitingContext, probe: ConsumptionProbe) {
        for (handler in handlers) {
            if (handler.handle(this, context, probe)) {
                return
            }
        }

        throwInternal("Unsupported context: ${context.javaClass.name}")
    }

    suspend fun onInteractionRateLimit(
        event: IReplyCallback,
        probe: ConsumptionProbe,
        messageGetter: (ConsumptionProbe) -> MessageCreateData,
    ) {
        val content = messageGetter(probe)
        val hook = event.reply(content).setEphemeral(true).await()
        // Only schedule delete if the interaction hook doesn't expire before
        // Technically this is supposed to be 15 minutes but, just to be safe
        if (deleteOnRefill && probe.nanosToWaitForRefill <= 10.minutes.inWholeNanoseconds) {
            deleteScope.launch {
                delay(probe.nanosToWaitForRefill.nanoseconds)
                hook.deleteOriginal().awaitCatching()
            }
        }
    }

    fun interface RequestHandler {
        suspend fun handle(instance: DefaultRateLimitHandler, context: RateLimitingContext, probe: ConsumptionProbe): Boolean
    }

    companion object {
        val deleteScope = namedDefaultScope("Rate limit message delete", 1, isDaemon = true)
    }
}
