package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.internal.modals.ModalDSL
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

@ModalDSL
abstract class TextInputBuilder internal constructor(
    label: String?,
    style: TextInputStyle?
) : TextInput.Builder("0", label, style) {
    /**
     * An ID is already generated automatically, but you can set a custom ID if you wish to.
     *
     * **Tip:** A modal input with the same ID as a previously sent one, will have the previously submitted values.
     */
    override fun setId(customId: String): TextInputBuilder = this.apply { super.setId(customId) }

    override fun setLabel(label: String): TextInputBuilder = this.apply { super.setLabel(label) }

    override fun setStyle(style: TextInputStyle?): TextInputBuilder = this.apply { super.setStyle(style) }

    override fun setRequired(required: Boolean): TextInputBuilder = this.apply { super.setRequired(required) }

    override fun setMinLength(minLength: Int): TextInputBuilder = this.apply { super.setMinLength(minLength) }

    override fun setMaxLength(maxLength: Int): TextInputBuilder = this.apply { super.setMaxLength(maxLength) }

    override fun setRequiredRange(min: Int, max: Int): TextInputBuilder = this.apply { super.setRequiredRange(min, max) }

    override fun setValue(value: String?): TextInputBuilder = this.apply { super.setValue(value) }

    override fun setPlaceholder(placeholder: String?): TextInputBuilder = this.apply { super.setPlaceholder(placeholder) }

    protected fun jdaBuild(): TextInput {
        return super.build()
    }

    abstract override fun build(): TextInput
}
