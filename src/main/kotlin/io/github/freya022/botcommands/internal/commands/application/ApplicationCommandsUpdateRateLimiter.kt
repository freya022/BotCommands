package io.github.freya022.botcommands.internal.commands.application

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.SequentialRestRateLimiter
import net.dv8tion.jda.internal.JDAImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

internal interface ApplicationCommandsUpdateRateLimiter {
    suspend fun awaitToken()
}

private object NullApplicationCommandsUpdateRateLimiter : ApplicationCommandsUpdateRateLimiter {
    override suspend fun awaitToken() {}
}

private class DefaultApplicationCommandsUpdateRateLimiter : ApplicationCommandsUpdateRateLimiter {
    // Whatever, there will be no code running on it at all, apart from resuming the coroutine
    private val updateRateLimitScheduler = Executors.newSingleThreadScheduledExecutor {
        thread(name = "Command update RateLimiter", start = false, isDaemon = true) {}
    }
    // 20/s
    private val updateBucket = Bucket.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(20)
                .refillIntervally(20, 1.seconds.toJavaDuration())
                .build()
        )
        .build()
        .asScheduler()

    override suspend fun awaitToken() {
        updateBucket.consume(1, updateRateLimitScheduler).await()
    }
}

@BService
@Configuration
internal open class ApplicationCommandsUpdateRateLimiterProvider {
    @Lazy
    @Bean
    @BService
    open fun applicationCommandsUpdateRateLimiter(jda: JDA): ApplicationCommandsUpdateRateLimiter {
        if (jda !is JDAImpl) return NullApplicationCommandsUpdateRateLimiter

        return when (jda.requester.rateLimiter) {
            is SequentialRestRateLimiter -> DefaultApplicationCommandsUpdateRateLimiter()
            else -> NullApplicationCommandsUpdateRateLimiter
        }
    }
}