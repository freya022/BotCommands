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

    /** Whether this select menu should be disabled, defaults to `false` */
    var disabled: Boolean
        get() = builder.isDisabled
        set(value) {
            builder.isDisabled = value
        }

    /** Displayed when no selections have been made yet, see [net.dv8tion.jda.api.components.selections.SelectMenu.Builder.placeholder] */
    var placeholder: String?
        get() = builder.placeholder
        set(value) {
            builder.placeholder = value
        }

    /** The minimum and maximum amount of values a user can select, must not exceed the amount of options */
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

    /** The maximum amount of values a user can select, must not exceed the amount of options */
    var maxValues: Int
        get() = builder.maxValues
        set(value) {
            builder.setMaxValues(value)
        }
}
