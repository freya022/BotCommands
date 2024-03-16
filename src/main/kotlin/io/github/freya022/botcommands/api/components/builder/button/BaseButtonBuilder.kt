package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import javax.annotation.CheckReturnValue

class BaseButtonBuilder internal constructor(
    private val componentController: ComponentController,
    private val style: ButtonStyle,
    private val label: String?,
    private val emoji: Emoji?
) {
    @CheckReturnValue
    fun ephemeral(): EphemeralButtonBuilder =
        EphemeralButtonBuilder(componentController, style, label, emoji, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun ephemeral(block: EphemeralButtonBuilder.() -> Unit): Button =
        ephemeral().apply(block).buildSuspend()

    @CheckReturnValue
    fun persistent(): PersistentButtonBuilder =
        PersistentButtonBuilder(componentController, style, label, emoji, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun persistent(block: PersistentButtonBuilder.() -> Unit): Button =
        persistent().apply(block).buildSuspend()
}