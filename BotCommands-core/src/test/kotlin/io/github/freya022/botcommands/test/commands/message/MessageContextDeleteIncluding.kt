package io.github.freya022.botcommands.test.commands.message

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.deleteDelayed
import dev.freya02.botcommands.jda.ktx.messages.editMessage
import dev.freya02.botcommands.jda.ktx.messages.editMessage_
import dev.freya02.botcommands.jda.ktx.messages.send
import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.test.commands.slash.ActionRow
import kotlinx.coroutines.future.await
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Command
class MessageContextDeleteIncluding(
    private val buttons: Buttons,
) : ApplicationCommand() {

    @BotPermissions(Permission.MESSAGE_MANAGE)
    @UserPermissions(Permission.MESSAGE_MANAGE)
    @JDAMessageCommand(name = "Delete messages including this")
    suspend fun onMessageDeleteMessagesIncludingThis(event: GuildMessageEvent, @ContextOption message: Message) {
        event.deferReply(true).queue()

        val dateLimit = (Clock.System.now() - 14.days).toJavaInstant()
        val messagesToDelete = message.channel.iterableHistory
            .takeUntilAsync { it.idLong < message.idLong || it.timeCreated.toInstant() < dateLimit }
            .await()

        // Reduce memory usage of captured data
        val messageIdsToDelete = LongArray(messagesToDelete.size) { messagesToDelete[it].idLong }

        event.hook.send {
            content = "This will delete ${messagesToDelete.size} messages up until ${messagesToDelete.last().jumpUrl}"

            // TODO use CV2 DSL for the ActionRow
            components += row(
                buttons.danger("Delete").ephemeral {
                    singleUse = true

                    bindTo { buttonEvent ->
                        val futures = message.channel.purgeMessagesById(*messageIdsToDelete)
                        buttonEvent.editMessage_ {
                            content = "Deleting messages..."
                        }.queue()

                        futures.forEach { it.await() }

                        buttonEvent.hook
                            .editMessage {
                                content = "Done!"
                            }
                            .deleteDelayed(2.seconds)
                            .await()
                    }
                }
            )
        }.queue()
    }
}
