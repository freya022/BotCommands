package io.github.freya022.botcommands.api.modals

import dev.freya02.botcommands.jda.ktx.components.InlineComponent
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle

class InlineTextInput(
    val builder: TextInputBuilder,
) : InlineComponent {

    override var uniqueId: Int
        get() = builder.uniqueId
        set(value) {
            builder.setUniqueId(value)
        }

    /** Style for the text input, see [TextInputBuilder.setStyle] */
    var style: TextInputStyle
        get() = builder.style
        set(value) {
            builder.setStyle(value)
        }

    /** Whether the user is required to write in this TextInput, see [TextInputBuilder.setRequired] */
    var isRequired: Boolean
        get() = builder.isRequired
        set(value) {
            builder.setRequired(value)
        }

    /** Minimum and maximum required length of this TextInput, see [TextInputBuilder.setRequiredRange] */
    var range: IntRange
        get() = when {
            builder.minLength == -1 && builder.maxLength == -1 -> IntRange.EMPTY
            builder.minLength != -1 -> builder.minLength..Int.MAX_VALUE
            builder.maxLength != -1 -> 0..builder.maxLength
            else -> builder.minLength..builder.maxLength
        }
        set(value) {
            builder.setRequiredRange(value)
        }

    /** Minimum required length of this TextInput, see [TextInputBuilder.setMinLength] */
    var minLength: Int
        get() = builder.minLength
        set(value) {
            builder.setMinLength(value)
        }

    /** Maximum required length of this TextInput, see [TextInputBuilder.setMaxLength] */
    var maxLength: Int
        get() = builder.maxLength
        set(value) {
            builder.setMaxLength(value)
        }

    /** Pre-populated text for this TextInput field, see [TextInputBuilder.setValue] */
    var value: String?
        get() = builder.value
        set(value) {
            builder.value = value
        }

    /** Short hint that describes the expected value of the input field, see [TextInputBuilder.setPlaceholder] */
    var placeholder: String?
        get() = builder.placeholder
        set(value) {
            builder.placeholder = value
        }

    fun build(): TextInput {
        return builder.build()
    }
}
