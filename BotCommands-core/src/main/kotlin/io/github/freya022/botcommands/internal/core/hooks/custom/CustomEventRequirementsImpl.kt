package io.github.freya022.botcommands.internal.core.hooks.custom

import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirements
import io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents
import io.github.freya022.botcommands.api.core.utils.toEnumSet
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import net.dv8tion.jda.api.requests.GatewayIntent

@OptIn(ExperimentalCustomEvents::class)
internal class CustomEventRequirementsImpl(
    intents: Collection<GatewayIntent>,
) : CustomEventRequirements {

    private val intents: Set<GatewayIntent> = intents.toEnumSet().unmodifiableView()

    override fun isEmpty(): Boolean = intents.isEmpty()

    override fun areUnknown(): Boolean = false

    override fun getIntents(): Set<GatewayIntent> = intents
}
