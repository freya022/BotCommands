package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent

/**
 * Construct an [ActionRow] from the provided components
 */
@ReplaceJdaKtx
fun row(vararg components: ActionRowChildComponent) = ActionRow.of(*components)

/**
 * Construct an [ActionRow] from the provided components
 */
@ReplaceJdaKtx
fun Collection<ActionRowChildComponent>.row() = ActionRow.of(this)

@ReplaceJdaKtx("dev.minn.jda.ktx.messages")
@JvmName("intoComponents")
fun <T : ActionRowChildComponent> Collection<T>.into(): List<ActionRow> = listOf(this.row())

@ReplaceJdaKtx("dev.minn.jda.ktx.messages")
fun ActionRowChildComponent.into(): List<ActionRow> = row(this).into()

@ReplaceJdaKtx("dev.minn.jda.ktx.messages")
fun <T : ActionRow> T.into(): List<T> = listOf(this)
