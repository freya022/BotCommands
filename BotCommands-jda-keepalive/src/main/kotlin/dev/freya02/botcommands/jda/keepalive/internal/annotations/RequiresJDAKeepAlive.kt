package dev.freya02.botcommands.jda.keepalive.internal.annotations

import dev.freya02.botcommands.jda.keepalive.api.config.JDAKeepAliveConfig
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies

@Dependencies(JDAKeepAliveConfig::class)
internal annotation class RequiresJDAKeepAlive
