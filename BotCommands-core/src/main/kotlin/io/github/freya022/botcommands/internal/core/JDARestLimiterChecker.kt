package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.events.InjectedJDAEvent
import io.github.freya022.botcommands.api.core.requests.PriorityGlobalRestRateLimiter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.SequentialRestRateLimiter
import net.dv8tion.jda.internal.JDAImpl

private val logger = KotlinLogging.logger { }

@BService
internal class JDARestLimiterChecker(private val config: BConfig) {

    @BEventListener
    fun onJDA(event: InjectedJDAEvent) {
        if (config.ignoreRestRateLimiter) return
        if (User.UserFlag.VERIFIED_BOT !in event.jda.selfUser.flags) return

        val jda = event.jda as? JDAImpl ?: return
        if (jda.requester.rateLimiter is SequentialRestRateLimiter) {
            logger.warn {
                "The default REST rate limiter is not recommended for verified bots, I recommend using ${classRef<PriorityGlobalRestRateLimiter>()} or your own implementation. " +
                        "You can also disable this message using ${BConfigBuilder::ignoreRestRateLimiter.reference}"
            }
        }
    }
}