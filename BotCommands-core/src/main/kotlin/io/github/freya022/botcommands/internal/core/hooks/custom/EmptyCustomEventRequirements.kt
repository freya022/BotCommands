package io.github.freya022.botcommands.internal.core.hooks.custom

import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirements
import io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents
import net.dv8tion.jda.api.requests.GatewayIntent

@OptIn(ExperimentalCustomEvents::class)
internal object EmptyCustomEventRequirements : CustomEventRequirements {

    override fun isEmpty(): Boolean = true

    override fun areUnknown(): Boolean = false

    override fun getIntents(): Set<GatewayIntent> = emptySet()
}
