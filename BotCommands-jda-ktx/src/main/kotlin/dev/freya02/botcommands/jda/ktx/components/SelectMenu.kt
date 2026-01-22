package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.ranges.setRequiredRange
import net.dv8tion.jda.api.components.selections.SelectMenu

abstract class InlineSelectMenu : InlineComponent {

    abstract val builder: SelectMenu.Builder<*, *>

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

    /** Whether this select menu should be disabled, cannot be `true` in modals, defaults to `false` */
    var disabled: Boolean
        get() = builder.isDisabled
        set(value) {
            builder.isDisabled = value
        }

    /**
     * Whether the user must populate this select menu if inside a Modal.
     *
     * This defaults to `true` when this is used in a Modal.
     *
     * See [SelectMenu.Builder.setRequired].
     */
    var required: Boolean?
        get() = builder.isRequired
        set(value) {
            builder.isRequired = value
        }

    /** Displayed when no selections have been made yet, see [SelectMenu.Builder.setPlaceholder] */
    var placeholder: String?
        get() = builder.placeholder
        set(value) {
            builder.placeholder = value
        }

    /** The minimum and maximum amount of values a user can select, must not exceed [SelectMenu.OPTIONS_MAX_AMOUNT] */
    var valueRange: IntRange
        get() = builder.minValues..builder.maxValues
        set(value) {
            builder.setRequiredRange(value)
        }

    /** The minimum amount of values a user can select, default to `1` */
    var minValues: Int
        get() = builder.minValues
        set(value) {
            builder.setMinValues(value)
        }

    /** The maximum amount of values a user can select, must not exceed [SelectMenu.OPTIONS_MAX_AMOUNT] */
    var maxValues: Int
        get() = builder.maxValues
        set(value) {
            builder.setMaxValues(value)
        }
}
