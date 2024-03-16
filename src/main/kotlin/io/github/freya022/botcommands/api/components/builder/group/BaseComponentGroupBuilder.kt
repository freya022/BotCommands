package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.api.components.IdentifiableComponent
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import javax.annotation.CheckReturnValue

class BaseComponentGroupBuilder internal constructor(
    private val componentController: ComponentController,
    private val components: Array<out IdentifiableComponent>
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