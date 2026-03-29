package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.components.utils.checkInit
import net.dv8tion.jda.api.components.checkbox.Checkbox

private val DUMMY_CHECKBOX = Checkbox.of("id")

class InlineCheckbox : InlineComponent {

    private var component: Checkbox = DUMMY_CHECKBOX

    override var uniqueId: Int
        get() = component.uniqueId
        set(value) {
            component = component.withUniqueId(value)
        }

    private var _customId: String? = null
    /** The custom ID, it can be used to pass data, then be read from an interaction */
    var customId: String
        get() = _customId.checkInit("custom ID")
        set(value) {
            component = component.withCustomId(value)
            _customId = value
        }

    /**
     * Whether this checkbox is selected by default.
     */
    var isDefault: Boolean
        get() = component.isDefault
        set(value) {
            component = component.withDefault(value)
        }

    fun build(): Checkbox {
        customId.checkInit()
        return component
    }
}

/**
 * A component displaying a box which can be checked. Useful for simple yes/no questions.
 *
 * @param customId    Custom identifier of this component, see [Checkbox.withCustomId]
 * @param uniqueId    Unique identifier of this component, see [Checkbox.withUniqueId]
 * @param isDefault   Whether it is checked by default
 * @param block       Lambda allowing further configuration
 *
 * @see net.dv8tion.jda.api.components.checkbox.Checkbox Checkbox
 */
inline fun Checkbox(
    customId: String,
    uniqueId: Int = -1,
    isDefault: Boolean = false,
    block: InlineCheckbox.() -> Unit = {},
): Checkbox {
    return InlineCheckbox()
        .apply {
            this.customId = customId
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            if (isDefault)
                this.isDefault = true
            block()
        }
        .build()
}
