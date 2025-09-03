package io.github.freya022.botcommands.api.modals

import dev.freya02.botcommands.jda.ktx.components.InlineComponent
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle

class InlineTextInput(
    val builder: TextInput.Builder,
) : InlineComponent {

    override var uniqueId: Int
        get() = builder.uniqueId
        set(value) {
            builder.setUniqueId(value)
        }

    /** Style for the text input, see [TextInput.Builder.setStyle] */
    var style: TextInputStyle
        get() = builder.style
        set(value) {
            builder.setStyle(value)
        }

    /** Whether the user is required to write in this TextInput, see [TextInput.Builder.setRequired] */
    var isRequired: Boolean
        get() = builder.isRequired
        set(value) {
            builder.setRequired(value)
        }

    /** Minimum and maximum required length of this TextInput, see [TextInput.Builder.setRequiredRange] */
    var range: IntRange
        get() = when {
            builder.minLength == -1 && builder.maxLength == -1 -> IntRange.EMPTY
            builder.minLength != -1 -> builder.minLength..Int.MAX_VALUE
            builder.maxLength != -1 -> 0..builder.maxLength
            else -> builder.minLength..builder.maxLength
        }
        set(value) {
            builder.setRequiredRange(value.first, value.last)
        }

    /** Minimum required length of this TextInput, see [TextInput.Builder.setMinLength] */
    var minLength: Int
        get() = builder.minLength
        set(value) {
            builder.setMinLength(value)
        }

    /** Maximum required length of this TextInput, see [TextInput.Builder.setMaxLength] */
    var maxLength: Int
        get() = builder.maxLength
        set(value) {
            builder.setMaxLength(value)
        }

    /** Pre-populated text for this TextInput field, see [TextInput.Builder.setValue] */
    var value: String?
        get() = builder.value
        set(value) {
            builder.value = value
        }

    /** Short hint that describes the expected value of the input field, see [TextInput.Builder.setPlaceholder] */
    var placeholder: String?
        get() = builder.placeholder
        set(value) {
            builder.placeholder = value
        }

    fun build(): TextInput {
        return builder.build()
    }
}

/**
 * Discord text input, see [TextInput][net.dv8tion.jda.api.components.textinput.TextInput].
 *
 * @param customId    The custom ID of the input, to retrieve this input with [@ModalInput][ModalInput], you must match against it
 * @param style       Style of text input
 * @param uniqueId    Unique identifier of this component, see [Component.withUniqueId]
 * @param range       Minimum and maximum required length of this TextInput, see [TextInput.Builder.setRequiredRange]
 * @param value       Pre-populated text for this TextInput field, see [TextInput.Builder.setValue]
 * @param placeholder Short hint that describes the expected value of the input field, see [TextInput.Builder.setPlaceholder]
 * @param block       Lambda allowing further configuration
 */
inline fun TextInput(
    customId: String,
    style: TextInputStyle,
    uniqueId: Int = -1,
    isRequired: Boolean = true,
    range: IntRange? = null,
    value: String? = null,
    placeholder: String? = null,
    block: InlineTextInput.() -> Unit = {},
): TextInput {
    return TextInput.create(customId, style)
        .let(::InlineTextInput)
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (!isRequired)
                this.isRequired = false
            if (range != null)
                this.range = range
            if (value != null)
                this.value = value
            if (placeholder != null)
                this.placeholder = placeholder
            block()
        }
        .build()
}

/**
 * Sets the minimum and maximum required length on this TextInput component.
 */
fun TextInput.Builder.setRequiredRange(range: IntRange) = setRequiredRange(range.first, range.last)
