package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.AwaitableComponent
import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.components.AbstractComponentImpl

internal abstract class AbstractAwaitableComponentImpl<T : ComponentInteraction> internal constructor(
    @get:JvmSynthetic
    internal val componentController: ComponentController,
    private val originalComponent: Component,
) : AbstractComponentImpl(),
    AwaitableComponent<T>,
    IGroupHolderMixin {

    @set:JvmSynthetic
    override var group: ComponentGroup? = null

    override fun toData(): DataObject {
        return (originalComponent as AbstractComponentImpl).toData()
    }

    @JvmSynthetic
    override suspend fun await(): T {
        check(group == null) {
            "Cannot await on a component owned by a group"
        }

        return componentController.continuationManager.awaitComponent(this)
    }
}
