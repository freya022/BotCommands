package io.github.freya022.botcommands.api.components.builder.select

import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralEntitySelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentEntitySelectBuilder
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import javax.annotation.CheckReturnValue

class BaseEntitySelectMenuBuilder internal constructor(
    private val componentController: ComponentController,
    private val targets: Collection<SelectTarget>
) {
    @CheckReturnValue
    fun ephemeral(): EphemeralEntitySelectBuilder =
        EphemeralEntitySelectBuilder(componentController, targets, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun ephemeral(block: EphemeralEntitySelectBuilder.() -> Unit): EntitySelectMenu =
        ephemeral().apply(block).buildSuspend()

    @CheckReturnValue
    fun persistent(): PersistentEntitySelectBuilder =
        PersistentEntitySelectBuilder(componentController, targets, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun persistent(block: PersistentEntitySelectBuilder.() -> Unit): EntitySelectMenu =
        persistent().apply(block).buildSuspend()
}