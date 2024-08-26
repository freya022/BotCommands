package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.AwaitableComponent
import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.ComponentInteraction

//TODO hide component internals properly
abstract class AbstractAwaitableComponent<T : ComponentInteraction> internal constructor(
    @get:JvmSynthetic
    internal val componentController: ComponentController
) : AwaitableComponent<T>,
    IGroupHolderMixin {

    @set:JvmSynthetic
    override var group: ComponentGroup? = null

    @JvmSynthetic
    override suspend fun await(): T {
        check(group == null) {
            "Cannot await on a component owned by a group"
        }

        return componentController.continuationManager.awaitComponent(this)
    }
}