package io.github.freya022.botcommands.api.components.builder.select

import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralStringSelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentStringSelectBuilder
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import javax.annotation.CheckReturnValue

class BaseStringSelectMenuBuilder internal constructor(
    private val componentController: ComponentController
) {
    @CheckReturnValue
    fun ephemeral(): EphemeralStringSelectBuilder =
        EphemeralStringSelectBuilder(componentController, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun ephemeral(block: EphemeralStringSelectBuilder.() -> Unit): StringSelectMenu =
        ephemeral().apply(block).buildSuspend()

    @CheckReturnValue
    fun persistent(): PersistentStringSelectBuilder =
        PersistentStringSelectBuilder(componentController, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun persistent(block: PersistentStringSelectBuilder.() -> Unit): StringSelectMenu =
        persistent().apply(block).buildSuspend()
}