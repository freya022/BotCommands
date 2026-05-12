package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.*
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration as JavaDuration
import kotlin.time.toKotlinDuration

@ConfigurationProperties(prefix = "botcommands.core", ignoreUnknownFields = false)
internal class BotCommandsCoreConfiguration(
    override val predefinedOwnerIds: Set<Long> = emptySet(),
    override val packages: Set<String> = emptySet(),
    override val classes: Set<Class<*>> = emptySet(),
    override val disableExceptionsInDMs: Boolean = false,
    override val enableOwnerBypass: Boolean = false,
    override val ignoredIntents: Set<GatewayIntent> = emptySet(),
    override val ignoreRestRateLimiter: Boolean = false,
    override val enableShutdownHook: Boolean = true,
) : AbstractBotCommandsConfiguration(), BConfigProps {
}

internal fun BConfigBuilder.applyConfig(configuration: BotCommandsCoreConfiguration) = apply {
    predefinedOwnerIds += configuration.predefinedOwnerIds
    packages += configuration.packages
    classes += configuration.classes
    disableExceptionsInDMs = configuration.disableExceptionsInDMs
    enableOwnerBypass = configuration.enableOwnerBypass
    ignoredIntents += configuration.ignoredIntents
    ignoreRestRateLimiter = configuration.ignoreRestRateLimiter
}

@ConfigurationProperties(prefix = "botcommands.event.manager", ignoreUnknownFields = false)
internal class BotCommandsEventManagerConfiguration(
    defaultTimeout: JavaDuration? = null,
) : AbstractBotCommandsConfiguration(), BEventManagerConfigProps {

    override val defaultTimeout = defaultTimeout?.toKotlinDuration()
}

@OptIn(DevConfig::class)
internal fun BEventManagerConfigBuilder.applyConfig(configuration: BotCommandsEventManagerConfiguration) = apply {
    defaultTimeout = configuration.defaultTimeout
}
