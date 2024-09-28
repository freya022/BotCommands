package io.github.freya022.botcommands.api.components.builder.select

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralEntitySelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentEntitySelectBuilder
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.select.ephemeral.EphemeralEntitySelectBuilderImpl
import io.github.freya022.botcommands.internal.components.builder.select.persistent.PersistentEntitySelectBuilderImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import javax.annotation.CheckReturnValue

/**
 * [EntitySelectMenu] factory provided by [SelectMenus].
 */
class EntitySelectMenuFactory internal constructor(
    private val componentController: ComponentController,
    private val targets: Collection<SelectTarget>
) {
    /**
     * Creates an ephemeral entity select menu builder.
     *
     * As a reminder, a [default timeout][Components.defaultTimeout] is set by default.
     *
     * @see Components
     */
    @CheckReturnValue
    fun ephemeral(): EphemeralEntitySelectBuilder =
        EphemeralEntitySelectBuilderImpl(componentController, targets, InstanceRetriever())

    /**
     * Creates an ephemeral entity select menu.
     *
     * As a reminder, a [default timeout][Components.defaultTimeout] is set by default.
     *
     * @see Components
     */
    @JvmSynthetic
    suspend inline fun ephemeral(block: EphemeralEntitySelectBuilder.() -> Unit): EntitySelectMenu =
        (ephemeral().apply(block) as EphemeralEntitySelectBuilderImpl).buildSuspend()

    /**
     * Creates a persistent entity select menu builder.
     *
     * As a reminder, **no timeout** is set by default.
     *
     * @see Components
     */
    @CheckReturnValue
    fun persistent(): PersistentEntitySelectBuilder =
        PersistentEntitySelectBuilderImpl(componentController, targets, InstanceRetriever())

    /**
     * Creates a persistent entity select menu.
     *
     * As a reminder, **no timeout** is set by default.
     *
     * @see Components
     */
    @JvmSynthetic
    suspend inline fun persistent(block: PersistentEntitySelectBuilder.() -> Unit): EntitySelectMenu =
        (persistent().apply(block) as PersistentEntitySelectBuilderImpl).buildSuspend()
}