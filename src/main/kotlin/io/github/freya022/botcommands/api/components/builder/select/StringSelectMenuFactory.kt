package io.github.freya022.botcommands.api.components.builder.select

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralStringSelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentStringSelectBuilder
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import javax.annotation.CheckReturnValue

/**
 * [StringSelectMenu] factory provided by [SelectMenus].
 */
class StringSelectMenuFactory internal constructor(
    private val componentController: ComponentController
) {
    /**
     * Creates an ephemeral string select menu builder.
     *
     * As a reminder, a [default timeout][Components.defaultTimeout] is set by default.
     *
     * @see Components
     */
    @CheckReturnValue
    fun ephemeral(): EphemeralStringSelectBuilder =
        EphemeralStringSelectBuilder(componentController, InstanceRetriever())

    /**
     * Creates an ephemeral string select menu.
     *
     * As a reminder, a [default timeout][Components.defaultTimeout] is set by default.
     *
     * @see Components
     */
    @JvmSynthetic
    suspend inline fun ephemeral(block: EphemeralStringSelectBuilder.() -> Unit): StringSelectMenu =
        ephemeral().apply(block).buildSuspend()

    /**
     * Creates a persistent string select menu builder.
     *
     * As a reminder, **no timeout** is set by default.
     *
     * @see Components
     */
    @CheckReturnValue
    fun persistent(): PersistentStringSelectBuilder =
        PersistentStringSelectBuilder(componentController, InstanceRetriever())

    /**
     * Creates a persistent string select menu.
     *
     * As a reminder, **no timeout** is set by default.
     *
     * @see Components
     */
    @JvmSynthetic
    suspend inline fun persistent(block: PersistentStringSelectBuilder.() -> Unit): StringSelectMenu =
        persistent().apply(block).buildSuspend()
}