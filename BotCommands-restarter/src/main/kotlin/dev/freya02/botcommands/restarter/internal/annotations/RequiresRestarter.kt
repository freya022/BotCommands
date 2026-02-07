package dev.freya02.botcommands.restarter.internal.annotations

import dev.freya02.botcommands.restarter.api.config.RestarterConfig
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.core.service.annotations.RequiresDefaultInjection

@Dependencies(RestarterConfig::class)
@RequiresDefaultInjection
internal annotation class RequiresRestarter
