package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.components.utils.MutableAccumulator
import net.dv8tion.jda.api.components.radiogroup.RadioGroup
import net.dv8tion.jda.api.components.radiogroup.RadioGroupOption

class InlineRadioGroup(val builder: RadioGroup.Builder) : InlineComponent {

    override var uniqueId: Int
        get() = builder.uniqueId
        set(value) {
            builder.uniqueId = value
        }

    /** The custom ID, it can be used to pass data, then be read from an interaction */
    var customId: String
        get() = builder.customId
        set(value) {
            builder.setCustomId(value)
        }

    /** Options of this select menu, see [RadioGroup.Builder.addOptions] */
    val options = MutableAccumulator(builder.options)

    /**
     * Whether the user must select an option.
     *
     * @see [RadioGroup.Builder.setRequired].
     */
    var required: Boolean
        get() = builder.isRequired
        set(value) {
            builder.isRequired = value
        }

    /**
     * Adds an option to this radio group.
     *
     * @param label       The label of this option, see [RadioGroupOption.withLabel]
     * @param value       The value of this option, this is what the bot receives, see [RadioGroupOption.withValue]
     * @param description The description of this option, see [RadioGroupOption.withDescription]
     * @param default     Whether this option is selected by default
     */
    fun option(
        label: String,
        value: String,
        description: String? = null,
        default: Boolean = false,
    ) {
        options += RadioGroupOption(label, value, description, default)
    }

    fun build(): RadioGroup {
        return builder.build()
    }
}

/**
 * A component displaying a group of up to [OPTIONS_MAX_AMOUNT][RadioGroup.OPTIONS_MAX_AMOUNT] radio buttons,
 * in which only one can be chosen.
 *
 * @param customId    Custom identifier of this component, see [RadioGroup.Builder.setCustomId]
 * @param uniqueId    Unique identifier of this component, see [RadioGroup.Builder.setUniqueId]
 * @param required    Whether the user must select an option
 * @param block       Lambda allowing further configuration
 *
 * @see net.dv8tion.jda.api.components.radiogroup.RadioGroup RadioGroup
 */
inline fun RadioGroup(
    customId: String,
    uniqueId: Int = -1,
    required: Boolean = true,
    block: InlineRadioGroup.() -> Unit,
): RadioGroup {
    return InlineRadioGroup(RadioGroup.create(customId))
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (!required)
                this.required = false
            block()
        }
        .build()
}
