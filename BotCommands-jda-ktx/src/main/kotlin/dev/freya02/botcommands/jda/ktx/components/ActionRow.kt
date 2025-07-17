package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent

/**
 * Construct an [ActionRow] from the provided components
 */
fun row(vararg components: ItemComponent) = ActionRow.of(*components)

/**
 * Construct an [ActionRow] from the provided components
 */
fun Collection<ItemComponent>.row() = ActionRow.of(this)
