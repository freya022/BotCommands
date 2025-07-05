package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.IGroupHolder
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.group.EphemeralComponentGroupBuilderImpl
import io.github.freya022.botcommands.internal.components.builder.group.PersistentComponentGroupBuilderImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import javax.annotation.CheckReturnValue

/**
 * [ComponentGroup] factory provided by [Components].
 */
class ComponentGroupFactory internal constructor(
    private val componentController: ComponentController,
    private val components: Array<out IGroupHolder>
) {
    /**
     * Creates an ephemeral component group builder.
     *
     * As a reminder, a [default timeout][Components.defaultEphemeralTimeout] is set.
     *
     * @see Components
     */
    @CheckReturnValue
    fun ephemeral(): EphemeralComponentGroupBuilder =
        EphemeralComponentGroupBuilderImpl(componentController, components, InstanceRetriever())

    /**
     * Creates an ephemeral component group.
     *
     * As a reminder, a [default timeout][Components.defaultEphemeralTimeout] is set.
     *
     * @see Components
     */
    @JvmSynthetic
    suspend inline fun ephemeral(block: EphemeralComponentGroupBuilder.() -> Unit): ComponentGroup =
        (ephemeral().apply(block) as EphemeralComponentGroupBuilderImpl).buildSuspend()

    /**
     * Creates a persistent component group builder.
     *
     * As a reminder, a [default timeout][Components.defaultPersistentTimeout] is set.
     *
     * @see Components
     */
    @CheckReturnValue
    fun persistent(): PersistentComponentGroupBuilder =
        PersistentComponentGroupBuilderImpl(componentController, components, InstanceRetriever())

    /**
     * Creates a persistent component group.
     *
     * As a reminder, a [default timeout][Components.defaultPersistentTimeout] is set.
     *
     * @see Components
     */
    @JvmSynthetic
    suspend inline fun persistent(block: PersistentComponentGroupBuilder.() -> Unit): ComponentGroup =
        (persistent().apply(block) as PersistentComponentGroupBuilderImpl).buildSuspend()
}