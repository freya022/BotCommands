package io.github.freya022.botcommands.internal.commands.text

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.annotations.CommandMarker
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.internal.commands.text.TextUtils.getSpacedPath
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission.*
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.requests.ErrorResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger { }
private val spacePattern = Regex("\\s+")

@Command
@ConditionalService(HelpCommand.ExistingHelpChecker::class)
@ConditionalOnMissingBean(IHelpCommand::class)
internal class HelpCommand internal constructor(
    private val context: BContextImpl,
    private val defaultMessagesFactory: DefaultMessagesFactory,
    private val textCommandsContext: TextCommandsContext,
    private val helpBuilderConsumer: HelpBuilderConsumer?
) : IHelpCommand, TextCommandProvider {
    internal object ExistingHelpChecker : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            // Try to get IHelpCommand interfaced services, except ours
            // If empty, then the user didn't provide one, in which case we can allow
            //Won't take HelpCommand into account
            val helpCommands = serviceContainer.getInterfacedServices<IHelpCommand>()
            return when {
                // A user-defined help command was found
                helpCommands.isNotEmpty() -> {
                    logger.debug { "Using a custom 'help' text command implementation" }
                    "An user supplied IHelpCommand interfaced service is already active (${helpCommands.first()::class.simpleNestedName})"
                }
                // No user-defined help command, try to use ours
                else -> when {
                    serviceContainer.getService<BTextConfig>().isHelpDisabled -> {
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
        event.sendWithEmbedFooterIcon(privateChannel, embed, event.failureReporter("Unable to send help message"))
            .awaitCatching()
            .onSuccess {
                if (hasReactionPermissions)
                    event.reactSuccess().awaitCatching()
                        .ignore(ErrorResponse.REACTION_BLOCKED)
                        .orThrow()
            }
            .handle(ErrorResponse.CANNOT_SEND_TO_USER) {
                if (event.channel.canTalk())
                    event.respond(defaultMessagesFactory.get(event).closedDMErrorMsg).await()
                else if (hasReactionPermissions)
                    event.message.addReaction(context.textConfig.dmClosedEmoji).await()
            }
            .ignore(ErrorResponse.REACTION_BLOCKED)
            .orThrow()
    }

    private suspend fun sendCommandHelp(event: BaseCommandEvent, commandInfo: TextCommandInfo, temporary: Boolean) {
        val member = event.member
        val usability = commandInfo.getUsability(member, event.guildChannel)
        if (usability.isNotVisible) {
            return event.respond("Command '" + commandInfo.path.getSpacedPath() + "' does not exist").awaitUnit()
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

            variation(HelpCommand::onTextHelpCommand) {
                option("commandStr", "command path") {
                    helpExample = "tag"
                }
            }

            variation(HelpCommand::onTextHelpFallback) //fallback
		}
    }
}