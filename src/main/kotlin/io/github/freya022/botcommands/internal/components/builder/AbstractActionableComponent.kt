package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.builder.IActionableComponent
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.components.builder.mixin.IActionableComponentMixin

internal abstract class AbstractActionableComponent<T : IActionableComponent<T>>(
    override val context: BContext,
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IActionableComponentMixin<T> {

    override val filters: MutableList<ComponentInteractionFilter<*>> = arrayListOf()

    override var rateLimitReference: ComponentRateLimitReference? = null

    override fun rateLimitReference(reference: ComponentRateLimitReference): T = applyInstance {
        this.rateLimitReference = reference
    }

    override fun addFilter(filter: ComponentInteractionFilter<*>): T = applyInstance {
        this.filters += filter
    }

    override fun addFilter(filterType: Class<out ComponentInteractionFilter<*>>): T =
        addFilter(context.getService(filterType))
}