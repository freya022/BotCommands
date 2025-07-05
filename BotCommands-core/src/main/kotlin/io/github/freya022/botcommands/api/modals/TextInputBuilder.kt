package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.internal.modals.ModalDSL
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

@ModalDSL
abstract class TextInputBuilder internal constructor(
    label: String?,
    style: TextInputStyle?
) : TextInput.Builder("0", label, style) {
    @Deprecated("Cannot set an ID on text inputs managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): TextInputBuilder = this.apply {
        if (customId == "0") return@apply // Super constructor call
        throw UnsupportedOperationException("Cannot set an ID on text inputs managed by the framework")
    }

    protected fun internetSetId(customId: String) {
        super.setId(customId)
    }

    override fun setLabel(label: String): TextInputBuilder = this.apply { super.setLabel(label) }

    override fun setStyle(style: TextInputStyle?): TextInputBuilder = this.apply { super.setStyle(style) }

    override fun setRequired(required: Boolean): TextInputBuilder = this.apply { super.setRequired(required) }

    override fun setMinLength(minLength: Int): TextInputBuilder = this.apply { super.setMinLength(minLength) }

    override fun setMaxLength(maxLength: Int): TextInputBuilder = this.apply { super.setMaxLength(maxLength) }

    /**
     * Sets the minimum and maximum required length on this TextInput component.
     */
    @JvmSynthetic
    fun setRequiredRange(range: IntRange) = setRequiredRange(range.first, range.last)

    override fun setRequiredRange(min: Int, max: Int): TextInputBuilder = this.apply { super.setRequiredRange(min, max) }

    override fun setValue(value: String?): TextInputBuilder = this.apply { super.setValue(value) }

    override fun setPlaceholder(placeholder: String?): TextInputBuilder = this.apply { super.setPlaceholder(placeholder) }

    protected fun jdaBuild(): TextInput {
        return super.build()
    }

    abstract override fun build(): TextInput
}
