package io.github.freya022.botcommands.internal.commands.text.autoconfigure

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.annotations.CommandMarker
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.internal.commands.spacedPath
import io.github.freya022.botcommands.internal.commands.text.TextUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission.*
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.requests.ErrorResponse
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.minutes

private val spacePattern = Regex("\\s+")

internal class DefaultHelpCommand internal constructor(
    private val context: BContext,
    private val messagesFactory: BotCommandsMessagesFactory,
    private val textCommandsContext: TextCommandsContext,
    private val helpBuilderConsumer: HelpBuilderConsumer?
) : IHelpCommand,
    TextCommandProvider {

    @CommandMarker
    suspend fun onTextHelpFallback(event: CommandEvent) {
        sendGlobalHelp(event)
    }

    @CommandMarker
    suspend fun onTextHelpCommand(event: BaseCommandEvent, commandStr: String) {
        val commandInfo = textCommandsContext.findTextCommand(spacePattern.split(commandStr))
            ?: return event.respond("Command '$commandStr' does not exist").awaitUnit()

        sendCommandHelp(event, commandInfo, temporary = false)
    }

    override suspend fun onInvalidCommandSuspend(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        sendCommandHelp(event, commandInfo, temporary = true)
    }

    private suspend fun sendGlobalHelp(event: BaseCommandEvent) {
        val privateChannel = event.author.openPrivateChannel().await()
        val builder = generateGlobalHelp(event.member, event.guildChannel)
        val embed = builder.build()

        val hasReactionPermissions = event.guild.selfMember.hasPermission(event.guildChannel, MESSAGE_ADD_REACTION, MESSAGE_HISTORY)
        event.sendWithEmbedFooterIcon(privateChannel, embed)
            .awaitCatching()
            .onSuccess {
                if (hasReactionPermissions)
                    event.reactSuccess().awaitCatching()
                        .ignore(ErrorResponse.REACTION_BLOCKED)
                        // Throw only if there was an exception we haven't handled/ignored.
                        // onSuccess doesn't catch exceptions, this is reported to sendGlobalHelp
                        .orThrow()
            }
            // Ignore and reply in channel/react if we can't send to DMs
            .handle(ErrorResponse.CANNOT_SEND_TO_USER) {
                if (event.channel.canTalk())
                    event.channel.sendMessage(messagesFactory.get(event).closedDirectMessages(event)).await()
                else if (hasReactionPermissions)
                    // May throw REACTION_BLOCKED
                    event.message.addReaction(context.textConfig.dmClosedEmoji).await()
            }
            // Ignore when the bot has been blocked
            .ignore(ErrorResponse.REACTION_BLOCKED)
            // Throw only if there was an exception we haven't handled/ignored
            .orThrow()
    }

    private suspend fun sendCommandHelp(event: BaseCommandEvent, commandInfo: TextCommandInfo, temporary: Boolean) {
        val member = event.member
        val usability = commandInfo.getUsability(member, event.guildChannel)
        if (usability.isNotVisible) {
            return event.respond("Command '" + commandInfo.path.spacedPath + "' does not exist").awaitUnit()
        }

        val embed = generateCommandHelp(event, commandInfo)
        if (temporary) {
            runIgnoringResponse(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.MISSING_ACCESS, ErrorResponse.MISSING_PERMISSIONS) {
                event.respond(embed.build())
                    .deleteDelayed(1.minutes)
                    .await()
            }
        } else {
            event.respond(embed.build()).queue()
        }
    }

    private fun generateGlobalHelp(member: Member, channel: GuildMessageChannel): EmbedBuilder {
        val builder = textCommandsContext.defaultEmbedSupplier.get()
        builder.setTimestamp(Instant.now())
        builder.setColor(member.colorRaw)

        textCommandsContext.rootCommands
            .filter { it.getUsability(member, channel).isVisible }
            .groupByTo(TreeMap(String.CASE_INSENSITIVE_ORDER)) { it.category }
            .forEach { (category, commands) ->
                val commandListStr =
                    commands.joinToString("\n") { "**${it.name}** : ${it.description ?: "No description"}" }
                builder.addField(category, commandListStr, false)
            }

        helpBuilderConsumer?.accept(builder, true, null)

        return builder
    }

    private fun generateCommandHelp(event: BaseCommandEvent, commandInfo: TextCommandInfo): EmbedBuilder {
        val builder = TextUtils.generateCommandHelp(commandInfo, event)
        builder.setTimestamp(Instant.now())
        builder.setColor(event.member.colorRaw)

        helpBuilderConsumer?.accept(builder, false, commandInfo)

        return builder
    }

    override fun declareTextCommands(manager: TextCommandManager) {
        manager.textCommand("help") {
            category = "Utils"
            description = "Gives help for a command"

            botPermissions = enumSetOf(VIEW_CHANNEL, MESSAGE_SEND)

            variation(DefaultHelpCommand::onTextHelpCommand) {
                option("commandStr", "command path") {
                    helpExample = "tag"
                }
            }

            variation(DefaultHelpCommand::onTextHelpFallback) //fallback
		}
    }
}
