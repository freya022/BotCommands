package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.components.utils.MutableAccumulator
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.entities.emoji.Emoji

class InlineStringSelectMenu(override val builder: StringSelectMenu.Builder) : InlineSelectMenu() {

    /** Options of this select menu, see [StringSelectMenu.Builder.addOptions] */
    val options = MutableAccumulator(builder.options)

    /**
     * Adds an option to this select menu, see [SelectOption].
     *
     * @param label       The label of this option, see [SelectOption.withLabel]
     * @param value       The value of this option, this is what the bot receives, see [SelectOption.withValue]
     * @param description The description of this option, see [SelectOption.withDescription]
     * @param emoji       The emoji of this option
     * @param default     Whether this option is selected by default
     */
    fun option(
        label: String,
        value: String,
        description: String? = null,
        emoji: Emoji? = null,
        default: Boolean = false,
    ) {
        options += SelectOption(label, value, description, emoji, default)
    }

    fun build(): StringSelectMenu {
        return builder.build()
    }
}

/**
 * Represents a selection of options, see [StringSelectMenu].
 *
 * @param customId    Custom identifier of this component, see [StringSelectMenu.Builder.setCustomId]
 * @param uniqueId    Unique identifier of this component, see [StringSelectMenu.Builder.setUniqueId]
 * @param placeholder Displayed when no selections have been made yet, see [StringSelectMenu.Builder.setPlaceholder]
 * @param valueRange  The minimum and maximum amount of values a user can select, must not exceed the amount of options
 * @param required    Whether the user must populate this select menu if inside a Modal
 * @param disabled    Whether this select menu should be disabled, cannot be `true` in modals
 * @param block       Lambda allowing further configuration
 */
inline fun StringSelectMenu(
    customId: String,
    uniqueId: Int = -1,
    placeholder: String? = null,
    valueRange: IntRange? = null,
    required: Boolean? = null,
    disabled: Boolean = false,
    block: InlineStringSelectMenu.() -> Unit,
): StringSelectMenu {
    return InlineStringSelectMenu(StringSelectMenu.create(customId))
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (placeholder != null)
                this.placeholder = placeholder
            if (valueRange != null)
                this.valueRange = valueRange
            if (required != null)
                this.required = required
            if (disabled)
                this.disabled = true
            block()
        }
        .build()
}
