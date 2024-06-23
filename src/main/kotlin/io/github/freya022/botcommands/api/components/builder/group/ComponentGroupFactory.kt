package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.IGroupHolder
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import javax.annotation.CheckReturnValue

/**
 * [ComponentGroup] factory provided by [Components].
 */
class ComponentGroupFactory internal constructor(
    private val componentController: ComponentController,
    private val components: Array<out IGroupHolder>
) {
    @CheckReturnValue
    fun ephemeral(): EphemeralComponentGroupBuilder =
        EphemeralComponentGroupBuilder(componentController, components, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun ephemeral(block: EphemeralComponentGroupBuilder.() -> Unit): ComponentGroup =
        ephemeral().apply(block).buildSuspend()

    @CheckReturnValue
    fun persistent(): PersistentComponentGroupBuilder =
        PersistentComponentGroupBuilder(componentController, components, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun persistent(block: PersistentComponentGroupBuilder.() -> Unit): ComponentGroup =
        persistent().apply(block).buildSuspend()
}