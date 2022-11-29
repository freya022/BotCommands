package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.new_components.builder.ButtonBuilder
import com.freya02.botcommands.internal.new_components.ComponentHandler
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

@Suppress("UNCHECKED_CAST")
internal abstract class ButtonBuilderImpl<T : ButtonBuilder<T>>(
    private val componentController: ComponentController,
    private val style: ButtonStyle
) : ButtonBuilder<T> {
    override val componentType: ComponentType = ComponentType.BUTTON

    final override var oneUse: Boolean = false
        private set
    final override var constraints: InteractionConstraints = InteractionConstraints()
        private set
    override val timeout: ComponentTimeout? = null
    override val handler: ComponentHandler? = null

    override fun oneUse(): T = this.also { oneUse = true } as T

    override fun constraints(block: InteractionConstraints.() -> Unit): T = this.also { constraints.apply(block) } as T

    override fun build(label: String?, emoji: Emoji?): Button {
        require(handler != null) {
            throwUser("A component handler needs to be set using #bindTo methods")
        }
        return Button.of(style, componentController.createComponent(this), label, emoji)
    }
}