package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent

/**
 * Construct an [ActionRow] from the provided components
 */
fun row(vararg components: ItemComponent) = ActionRow.of(*components)

/**
 * Construct an [ActionRow] from the provided components
 */
fun Collection<ItemComponent>.row() = ActionRow.of(this)

@JvmName("intoComponents")
fun <T : ItemComponent> Collection<T>.into(): List<ActionRow> = listOf(this.row())

fun ItemComponent.into(): List<ActionRow> = row(this).into()

fun <T : LayoutComponent> T.into(): List<T> = listOf(this)
