package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.components.utils.MutableAccumulator
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.entities.emoji.Emoji

class InlineStringSelectMenu(var builder: StringSelectMenu.Builder) : InlineComponent {

    override var uniqueId: Int
        get() = builder.uniqueId
        set(value) {
            builder.uniqueId = value
        }

    // TODO remove once JDA exposes getter
    private var _required: Boolean? = null
    /**
     * Whether the user must populate this select menu if inside a Modal.
     *
     * This defaults to `true` when this is used in a Modal.
     *
     * See [StringSelectMenu.Builder.setRequired].
     */
    var required: Boolean?
        get() = _required
        set(value) {
            builder.setRequired(value)
            _required = value
        }

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
 * Represents a selection of options, see [StringSelectMenu][net.dv8tion.jda.api.components.selections.StringSelectMenu].
 *
 * @param customId Custom identifier of this component, see [StringSelectMenu.Builder.setCustomId]
 * @param uniqueId Unique identifier of this component, see [StringSelectMenu.Builder.setUniqueId]
 * @param required Whether the user must populate this select menu if inside a Modal, see [StringSelectMenu.Builder.setRequired]
 * @param block    Lambda allowing further configuration
 */
inline fun StringSelectMenu(
    customId: String,
    uniqueId: Int = -1,
    required: Boolean? = null,
    block: InlineStringSelectMenu.() -> Unit = {},
): StringSelectMenu {
    return InlineStringSelectMenu(StringSelectMenu.create(customId))
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (required != null)
                this.required = required
            block()
        }
        .build()
}
