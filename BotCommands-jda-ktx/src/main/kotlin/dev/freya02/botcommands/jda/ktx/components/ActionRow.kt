package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent

/**
 * Construct an [ActionRow] from the provided components
 */
@ReplaceJdaKtx
fun row(vararg components: ItemComponent) = ActionRow.of(*components)

/**
 * Construct an [ActionRow] from the provided components
 */
@ReplaceJdaKtx
fun Collection<ItemComponent>.row() = ActionRow.of(this)

@ReplaceJdaKtx("dev.minn.jda.ktx.messages")
@JvmName("intoComponents")
fun <T : ItemComponent> Collection<T>.into(): List<ActionRow> = listOf(this.row())

@ReplaceJdaKtx("dev.minn.jda.ktx.messages")
fun ItemComponent.into(): List<ActionRow> = row(this).into()

@ReplaceJdaKtx("dev.minn.jda.ktx.messages")
fun <T : LayoutComponent> T.into(): List<T> = listOf(this)
