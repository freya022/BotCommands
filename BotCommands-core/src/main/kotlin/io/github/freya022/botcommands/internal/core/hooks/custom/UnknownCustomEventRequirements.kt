package io.github.freya022.botcommands.internal.core.hooks.custom

import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirements
import io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents
import net.dv8tion.jda.api.requests.GatewayIntent

@OptIn(ExperimentalCustomEvents::class)
internal object UnknownCustomEventRequirements : CustomEventRequirements {

    override fun isEmpty(): Boolean = false

    override fun areUnknown(): Boolean = true

    override fun getIntents(): Set<GatewayIntent> = emptySet()
}
