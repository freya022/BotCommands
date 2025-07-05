package io.github.freya022.bot.commands.slash

import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.annotations.*
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.builder.timeoutWith
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.core.utils.awaitCatching
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteractionHook
import kotlin.time.Duration.Companion.seconds

/**
 * A slash command replying a select menu with a button.
 *
 * The select menu choices are temporarily stored in-memory,
 * while there is a confirmation button to then use those choices.
 */
@Command
class SlashDelayedSelectMenu(
    private val buttons: Buttons,
    private val selectMenus: SelectMenus
) : ApplicationCommand() {
    private class Choices {
        var values: List<String> = emptyList()
            private set
        lateinit var hook: LocalizableInteractionHook
            private set

        fun updateValues(interaction: StringSelectEvent) {
            values = interaction.values
            hook = interaction.hook
        }
    }

    private val choices: MutableMap<Long, Choices> = hashMapOf()

    @JDASlashCommand(name = "delayed_select_menu")
    suspend fun onSlashDelayedSelectMenu(event: GuildSlashEvent) {
        val interactionId = event.idLong
        val selectMenu = selectMenus.stringSelectMenu().persistent {
            options += List(3) { i -> SelectOption("Option $i", "$i") }

            bindWith(::onStringSelect, interactionId)
        }
        val confirmButton = buttons.success("Confirm").persistent {
            singleUse = true
            bindWith(::onConfirmClick, interactionId)
        }
        // Group the components together, so when the confirmation button is clicked,
        // it will be invalidated with the rest of the group.
        val group = buttons.group(selectMenu, confirmButton).persistent {
            resetTimeoutOnUse = true
            timeoutWith(10.seconds, ::onDelayedSelectMenuTimeout, interactionId)
        }

        event.replyComponents(row(selectMenu), row(confirmButton))
            .setEphemeral(true)
            .awaitCatching()
            .onFailure {
                buttons.deleteComponents(group)
            }
            .orThrow()
        choices[interactionId] = Choices()
    }

    @JDASelectMenuListener
    fun onStringSelect(event: StringSelectEvent, @ComponentData id: Long) {
        val choices = choices[id] ?: return event.reply_("Invalid menu", ephemeral = true).queue()
        choices.updateValues(event)

        event.deferEdit().queue()
    }

    @JDAButtonListener
    fun onConfirmClick(event: ButtonEvent, @ComponentData id: Long) {
        val choices = choices[id] ?: return event.reply_("Invalid menu", ephemeral = true).queue()
        if (choices.values.isEmpty())
            return event.reply_("Please select at least 1 choice", ephemeral = true).queue()

        event.editMessage("You selected ${choices.values}")
            .setReplace(true)
            .queue()
    }

    @GroupTimeoutHandler
    fun onDelayedSelectMenuTimeout(data: GroupTimeoutData, @TimeoutData id: Long) {
        val choices = choices.remove(id) ?: return // Bot may have restarted
        choices.hook.editOriginal("Timeout")
            .setReplace(true)
            .queue()
    }
}