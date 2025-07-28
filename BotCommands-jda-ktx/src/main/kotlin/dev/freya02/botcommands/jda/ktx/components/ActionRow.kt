package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji

private val DUMMY_ROW = ActionRow.of(Button.success("id", "label"))

class InlineActionRow : InlineComponentWithChildren<ActionRowChildComponent> {

    private var row = DUMMY_ROW

    override var uniqueId: Int
        get() = row.uniqueId
        set(value) {
            row = row.withUniqueId(value)
        }

    override val components = mutableListOf<ActionRowChildComponent>()

    fun link(url: String, label: String? = null, emoji: Emoji? = null, disabled: Boolean = false) {
        components += Button.of(ButtonStyle.LINK, url, label, emoji).withDisabled(disabled)
    }

    fun build(): ActionRow {
        return row.withComponents(components)
    }
}

/**
 * See [ActionRow][net.dv8tion.jda.api.components.actionrow.ActionRow].
 *
 * @param uniqueId    Unique identifier of this component
 * @param block       Lambda allowing further configuration
 *
 * @see ActionRowChildComponent
 */
inline fun ActionRow(uniqueId: Int = -1, block: InlineActionRow.() -> Unit): ActionRow =
    InlineActionRow()
        .apply {
            if (uniqueId != -1)
                this.uniqueId = uniqueId
            block()
        }
        .build()

/**
 * Construct an [ActionRow] from the provided components
 */
@ReplaceJdaKtx
fun row(component: ActionRowChildComponent, vararg components: ActionRowChildComponent) = ActionRow.of(component, *components)

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
