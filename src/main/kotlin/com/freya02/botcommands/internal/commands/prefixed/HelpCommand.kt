package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.CommandEvent
import com.freya02.botcommands.api.commands.prefixed.IHelpCommand
import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration
import com.freya02.botcommands.api.core.config.BTextConfig
import com.freya02.botcommands.api.core.service.ConditionalServiceChecker
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import com.freya02.botcommands.api.core.service.annotations.ServiceName
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.api.core.service.getInterfacedServices
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.Usability
import com.freya02.botcommands.internal.commands.prefixed.TextUtils.getSpacedPath
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.utils.ReflectionUtils.referenceString
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger { }
private val spacePattern = Regex("\\s+")

@Command
@ServiceName("builtinHelpCommand")
@ServiceType(IHelpCommand::class)
@ConditionalService(HelpCommand.ExistingHelpChecker::class)
internal class HelpCommand internal constructor(private val context: BContextImpl) : IHelpCommand {
    internal object ExistingHelpChecker : ConditionalServiceChecker {
        override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>): String? {
            // Try to get IHelpCommand interfaced services, except ours
            // If empty, then the user didn't provide one, in which case we can allow
            //Won't take HelpCommand into account
            val helpCommands = context.serviceContainer.getInterfacedServices<IHelpCommand>()
            return when {
                // A user-defined help command was found
                helpCommands.isNotEmpty() -> {
                    logger.debug("Using a custom 'help' text command implementation")
                    "An user supplied IHelpCommand interfaced service is already active (${helpCommands.first()::class.simpleNestedName})"
                }
                // No user-defined help command, try to use ours
                else -> when {
                    context.textConfig.isHelpDisabled -> {
                        logger.debug("Using no 'help' text command implementation")
                        "The help command was disabled in ${BTextConfig::isHelpDisabled.referenceString}"
                    }
                    else -> {
                        logger.debug("Using built-in help command")
                        null
                    }
                }
            }
        }
    }

    @CommandMarker
    suspend fun onTextHelpFallback(event: CommandEvent) {
        sendGlobalHelp(event)
    }

    @CommandMarker
    suspend fun onTextHelpCommand(event: BaseCommandEvent, commandStr: String) {
        val commandInfo = context.textCommandsContext.findTextCommand(spacePattern.split(commandStr))
        if (commandInfo == null) {
            event.respond("Command '$commandStr' does not exist").await()
            return
        }

        sendCommandHelp(event, commandInfo)
    }

    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        context.coroutineScopesConfig.textCommandsScope.launch {
            sendCommandHelp(event, commandInfo)
        }
    }

    private suspend fun sendGlobalHelp(event: BaseCommandEvent) {
        val builder = generateGlobalHelp(event.member, event.guildChannel)
        val embed = builder.build()

        try {
            event.author
                .openPrivateChannel()
                .flatMap { event.sendWithEmbedFooterIcon(it, embed, event.failureReporter("Unable to send help message")) }
                .await()

            event.reactSuccess().queue()
        } catch (e: ErrorResponseException) {
            when (e.errorResponse) {
                ErrorResponse.CANNOT_SEND_TO_USER -> event.respond(context.getDefaultMessages(event.guild).closedDMErrorMsg).queue()
                else -> throw e
            }
        }
    }

    private suspend fun sendCommandHelp(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        val member = event.member
        val usability = Usability.of(context, commandInfo, member, event.guildChannel, !context.isOwner(member.idLong))
        if (usability.isNotShowable) {
            event.respond("Command '" + commandInfo.path.getSpacedPath() + "' does not exist").await()
            return
        }

        val embed = generateCommandHelp(event, commandInfo)
        event.respond(embed.build()).queue()
    }

    private fun generateGlobalHelp(member: Member, channel: GuildMessageChannel): EmbedBuilder {
        val builder = context.defaultEmbedSupplier.get()
        builder.setTimestamp(Instant.now())
        builder.setColor(member.colorRaw)
        builder.setFooter("NSFW commands might not be shown\nRun help in an NSFW channel to see them\n")

        val categoryBuilderMap = TreeMap<String, StringJoiner>(String.CASE_INSENSITIVE_ORDER)
        for (cmd in context.textCommandsContext.rootCommands) {
            if (Usability.of(context, cmd, member, channel, !context.isOwner(member.idLong)).isShowable) {
                categoryBuilderMap
                    .computeIfAbsent(cmd.category) { StringJoiner("\n") }
                    .add("**${cmd.name}** : ${cmd.description}")
            }
        }

        for ((key, value) in categoryBuilderMap) {
            builder.addField(key, value.toString(), false)
        }

        context.helpBuilderConsumer?.accept(builder, true, null)

        return builder
    }

    private fun generateCommandHelp(event: BaseCommandEvent, commandInfo: TextCommandInfo): EmbedBuilder {
        val builder = TextUtils.generateCommandHelp(commandInfo, event)
        builder.setTimestamp(Instant.now())
        builder.setColor(event.member.colorRaw)

        context.helpBuilderConsumer?.accept(builder, false, commandInfo)

        return builder
    }

    @TextDeclaration
    internal fun declare(manager: TextCommandManager) {
		manager.textCommand("help") {
            category = "Utils"
            description = "Gives help for a command"

            variation(HelpCommand::onTextHelpCommand) {
                option("commandStr", "command path") {
                    helpExample = "tag"
                }
            }

            variation(HelpCommand::onTextHelpFallback) //fallback
		}
    }
}