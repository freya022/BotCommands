package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.components.utils.ObservableAccumulator
import dev.freya02.botcommands.jda.ktx.components.utils.enumSetOf
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.entities.channel.ChannelType

class InlineEntitySelectMenu(override val builder: EntitySelectMenu.Builder, types: Collection<EntitySelectMenu.SelectTarget>) : InlineSelectMenu() {

    /**
     * For select menus with the [CHANNEL][EntitySelectMenu.SelectTarget.CHANNEL] type,
     * the [ChannelTypes][ChannelType] that should be supported by this menu, see [EntitySelectMenu.Builder.setChannelTypes].
     */
    val channelTypes = ObservableAccumulator<ChannelType>(emptyList(), builder::setChannelTypes)

    /** The [SelectTargets][EntitySelectMenu.SelectTarget] that should be supported by this menu, see [EntitySelectMenu.Builder.setEntityTypes] */
    val types = ObservableAccumulator(types, builder::setEntityTypes)

    /** The default values that will be shown to the user, see [EntitySelectMenu.Builder.setDefaultValues] */
    val defaultValues = ObservableAccumulator<EntitySelectMenu.DefaultValue>(emptyList(), builder::setDefaultValues)

    fun build(): EntitySelectMenu {
        return builder.build()
    }
}

/**
 * Represents a selection of options, see [EntitySelectMenu].
 *
 * @param customId      Custom identifier of this component, see [EntitySelectMenu.Builder.setCustomId]
 * @param types         The [SelectTargets][EntitySelectMenu.SelectTarget] that should be supported by this menu, see [EntitySelectMenu.Builder.setEntityTypes]
 * @param uniqueId      Unique identifier of this component, see [EntitySelectMenu.Builder.setUniqueId]
 * @param placeholder   Displayed when no selections have been made yet, see [EntitySelectMenu.Builder.setPlaceholder]
 * @param valueRange    The minimum and maximum amount of values a user can select, must not exceed the amount of options
 * @param channelTypes  For select menus with the [CHANNEL][EntitySelectMenu.SelectTarget.CHANNEL] type,
 *   the [ChannelTypes][ChannelType] that should be supported by this menu,
 *   see [EntitySelectMenu.Builder.setChannelTypes]
 * @param defaultValues The default values that will be shown to the user, see [EntitySelectMenu.Builder.setDefaultValues]
 * @param required      Whether the user must populate this select menu if inside a Modal
 * @param disabled      Whether this select menu should be disabled, cannot be `true` in modals
 * @param block         Lambda allowing further configuration
 */
inline fun EntitySelectMenu(
    customId: String,
    types: Collection<EntitySelectMenu.SelectTarget>,
    uniqueId: Int = -1,
    placeholder: String? = null,
    valueRange: IntRange? = null,
    channelTypes: Collection<ChannelType> = emptyList(),
    defaultValues: Collection<EntitySelectMenu.DefaultValue> = emptyList(),
    required: Boolean? = null,
    disabled: Boolean = false,
    block: InlineEntitySelectMenu.() -> Unit = {},
): EntitySelectMenu {
    return InlineEntitySelectMenu(EntitySelectMenu.create(customId, types), types)
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
            this.channelTypes += channelTypes
            this.defaultValues += defaultValues
            block()
        }
        .build()
}

/**
 * Represents a selection of options, see [EntitySelectMenu].
 *
 * @param customId      Custom identifier of this component, see [EntitySelectMenu.Builder.setCustomId]
 * @param types         The [SelectTargets][EntitySelectMenu.SelectTarget] that should be supported by this menu, see [EntitySelectMenu.Builder.setEntityTypes]
 * @param uniqueId      Unique identifier of this component, see [EntitySelectMenu.Builder.setUniqueId]
 * @param placeholder   Displayed when no selections have been made yet, see [EntitySelectMenu.Builder.setPlaceholder]
 * @param valueRange    The minimum and maximum amount of values a user can select, must not exceed the amount of options
 * @param channelTypes  For select menus with the [CHANNEL][EntitySelectMenu.SelectTarget.CHANNEL] type,
 *   the [ChannelTypes][ChannelType] that should be supported by this menu,
 *   see [EntitySelectMenu.Builder.setChannelTypes]
 * @param defaultValues The default values that will be shown to the user, see [EntitySelectMenu.Builder.setDefaultValues]
 * @param required      Whether the user must populate this select menu if inside a Modal
 * @param disabled      Whether this select menu should be disabled, cannot be `true` in modals
 * @param block         Lambda allowing further configuration
 */
inline fun EntitySelectMenu(
    customId: String,
    type: EntitySelectMenu.SelectTarget,
    vararg types: EntitySelectMenu.SelectTarget,
    uniqueId: Int = -1,
    placeholder: String? = null,
    valueRange: IntRange? = null,
    channelTypes: Collection<ChannelType> = emptyList(),
    defaultValues: Collection<EntitySelectMenu.DefaultValue> = emptyList(),
    required: Boolean? = null,
    disabled: Boolean = false,
    block: InlineEntitySelectMenu.() -> Unit = {},
): EntitySelectMenu {
    val types = enumSetOf(type, *types)
    return EntitySelectMenu(customId, types, uniqueId, placeholder, valueRange, channelTypes, defaultValues, required, disabled, block)
}
