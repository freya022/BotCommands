package io.github.freya022.botcommands.internal.commands.prefixed

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.annotations.CommandMarker
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.CommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.IHelpCommand
import io.github.freya022.botcommands.api.commands.prefixed.TextCommandManager
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextDeclaration
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.delay
import io.github.freya022.botcommands.api.core.utils.handle
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.Usability
import io.github.freya022.botcommands.internal.commands.prefixed.TextUtils.getSpacedPath
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger { }
private val spacePattern = Regex("\\s+")

@Command
@ServiceName("builtinHelpCommand")
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
                    logger.debug { "Using a custom 'help' text command implementation" }
                    "An user supplied IHelpCommand interfaced service is already active (${helpCommands.first()::class.simpleNestedName})"
                }
                // No user-defined help command, try to use ours
                else -> when {
                    context.textConfig.isHelpDisabled -> {
                        logger.debug { "Using no 'help' text command implementation" }
                        "The help command was disabled in ${BTextConfig::isHelpDisabled.reference}"
                    }
                    else -> {
                        logger.debug { "Using built-in help command" }
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

        sendCommandHelp(event, commandInfo, temporary = false)
    }

    override fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo) {
        context.coroutineScopesConfig.textCommandsScope.launch {
            sendCommandHelp(event, commandInfo, temporary = true)
        }
    }

    private suspend fun sendGlobalHelp(event: BaseCommandEvent) {
        val builder = generateGlobalHelp(event.member, event.guildChannel)
        val embed = builder.build()

        runCatching {
            event.author
                .openPrivateChannel()
                .flatMap { event.sendWithEmbedFooterIcon(it, embed, event.failureReporter("Unable to send help message")) }
                .await()

            event.reactSuccess().queue()
        }.handle(ErrorResponse.CANNOT_SEND_TO_USER) {
            event.respond(context.getDefaultMessages(event.guild).closedDMErrorMsg).queue()
        }.getOrThrow()
    }

    private suspend fun sendCommandHelp(event: BaseCommandEvent, commandInfo: TextCommandInfo, temporary: Boolean) {
        val member = event.member
        val usability = Usability.of(context, commandInfo, member, event.guildChannel, !context.isOwner(member.idLong))
        if (usability.isNotShowable) {
            event.respond("Command '" + commandInfo.path.getSpacedPath() + "' does not exist").await()
            return
        }

        val embed = generateCommandHelp(event, commandInfo)
        if (temporary) {
            event.respond(embed.build())
                .delay(1.minutes)
                .flatMap(Message::delete)
                .queue(null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.MISSING_ACCESS, ErrorResponse.MISSING_PERMISSIONS))
        } else {
            event.respond(embed.build()).queue()
        }
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
                    .add("**${cmd.name}** : ${cmd.description ?: "No description"}")
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