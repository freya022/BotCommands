package com.freya02.botcommands.test_kt.commands.message

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.core.service.annotations.BService
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.await
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import kotlin.time.Duration.Companion.minutes

@BService
class ReactionRoleService {
    fun addReactionRole(message: Message, role: Role, reactionId: String, isUnicode: Boolean): Unit {

    }
}

@Command
class MessageContextAddReactionRole(private val componentsService: Components,
                                    private val reactionRoleService: ReactionRoleService) : ApplicationCommand() {
    @JDAMessageCommand(name = "Add reaction role", defaultLocked = true)
    suspend fun onMessageContextAddReactionRole(event: GuildMessageEvent,
                                                @AppOption message: Message) {
        val roleSelectMenu = componentsService.ephemeralEntitySelectMenu(EntitySelectMenu.SelectTarget.ROLE) {
            timeout(1.minutes)
        }

        event.createReply(ephemeral = true) {
            embed {
                description = "Pick a role !"
            }

            components += row(roleSelectMenu)
        }.await()

        val role = withTimeoutOrNull(1.minutes) {
            roleSelectMenu.await().also { it.deferEdit().queue() }.mentions.roles.single()
        } ?: return event.replaceOriginal("Role select timeout").queue()

        if (!event.member.canInteract(role))
            return event.replaceOriginal("You cannot interact with this role !").queue()
        if (!event.guild.selfMember.canInteract(role))
            return event.replaceOriginal("The bot cannot interact with this role !").queue()

        event.replaceOriginal {
            content = "Please add the reaction you wish to use on the message"
            components += row(Button.link(message.jumpUrl, "Jump to message"))
        }.queue()

        val reaction = withTimeoutOrNull(2.minutes) {
            event.jda.await<MessageReactionAddEvent>() { it.messageIdLong == message.idLong && it.user == event.user }.reaction
        } ?: return event.replaceOriginal("Reaction select timeout").queue()

        message.addReaction(reaction.emoji).await()
        reaction.removeReaction(event.user).await()
        reactionRoleService.addReactionRole(message, role,
                reactionId = reaction.emoji.asReactionCode,
                isUnicode = reaction.emoji.type == Emoji.Type.UNICODE)

        event.replaceOriginal("Reaction role successfully set up").queue()
    }

    private fun IReplyCallback.createReply(ephemeral: Boolean = false, block: InlineMessage<MessageCreateData>.() -> Unit) =
        reply(MessageCreate { block() }).setEphemeral(ephemeral)

    private fun IDeferrableCallback.replaceOriginal(block: InlineMessage<MessageEditData>.() -> Unit) =
        hook.editOriginal(MessageEdit { block() }).setReplace(true)

    private fun IDeferrableCallback.replaceOriginal(content: String) =
        hook.editOriginal(content).setReplace(true)
}