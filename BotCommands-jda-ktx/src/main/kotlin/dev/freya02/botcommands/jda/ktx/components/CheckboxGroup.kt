package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.components.utils.MutableAccumulator
import dev.freya02.botcommands.jda.ktx.ranges.setRequiredRange
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroup
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroupOption

class InlineCheckboxGroup(val builder: CheckboxGroup.Builder) : InlineComponent {

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

    /** Options of this checkbox group, see [CheckboxGroup.Builder.addOptions] */
    val options = MutableAccumulator(builder.options)

    /**
     * Whether the user must select at least [the minimum amount of options][minValues].
     *
     * @see [CheckboxGroup.Builder.setRequired].
     */
    var required: Boolean
        get() = builder.isRequired
        set(value) {
            builder.isRequired = value
        }

    /** The minimum and maximum amount of values a user can select, must not exceed [CheckboxGroup.OPTIONS_MAX_AMOUNT] */
    var valueRange: IntRange
        get() = builder.minValues..builder.maxValues
        set(value) {
            builder.setRequiredRange(value)
        }

    /** The minimum amount of values a user must select, default to `1` */
    var minValues: Int
        get() = builder.minValues
        set(value) {
            builder.setMinValues(value)
        }

    /** The maximum amount of values a user can select, must not exceed [CheckboxGroup.OPTIONS_MAX_AMOUNT] */
    var maxValues: Int
        get() = builder.maxValues
        set(value) {
            builder.setMaxValues(value)
        }

    /**
     * Adds an option to this checkbox group.
     *
     * @param label       The label of this option, see [CheckboxGroupOption.withLabel]
     * @param value       The value of this option, this is what the bot receives, see [CheckboxGroupOption.withValue]
     * @param description The description of this option, see [CheckboxGroupOption.withDescription]
     * @param default     Whether this option is selected by default
     */
    fun option(
        label: String,
        value: String,
        description: String? = null,
        default: Boolean = false,
    ) {
        options += CheckboxGroupOption(label, value, description, default)
    }

    fun build(): CheckboxGroup {
        return builder.build()
    }
}

/**
 * A component displaying a group of up to [OPTIONS_MAX_AMOUNT][CheckboxGroup.OPTIONS_MAX_AMOUNT] checkboxes
 * which can be checked independently.
 *
 * @param customId    Custom identifier of this component, see [CheckboxGroup.Builder.setCustomId]
 * @param uniqueId    Unique identifier of this component, see [CheckboxGroup.Builder.setUniqueId]
 * @param valueRange  The minimum and maximum amount of values a user can select, must not exceed [CheckboxGroup.OPTIONS_MAX_AMOUNT]
 * @param required    Whether the user must populate at least the minimum amount of options
 * @param block       Lambda allowing further configuration
 *
 * @see net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroup
 */
inline fun CheckboxGroup(
    customId: String,
    uniqueId: Int = -1,
    valueRange: IntRange? = null,
    required: Boolean = true,
    block: InlineCheckboxGroup.() -> Unit,
): CheckboxGroup {
    return InlineCheckboxGroup(CheckboxGroup.create(customId))
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (valueRange != null)
                this.valueRange = valueRange
            if (!required)
                this.required = false
            block()
        }
        .build()
}
