package io.github.freya022.bot.commands.slash

import io.github.freya022.bot.commands.ban.BanService
import io.github.freya022.bot.resolvers.localize
import io.github.freya022.bot.switches.FrontendChooser
import io.github.freya022.bot.switches.SimpleFrontend
import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.awaitAny
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.utils.deleteDelayed
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.editLocalized
import io.github.freya022.botcommands.api.localization.context.replaceLocalized
import io.github.freya022.botcommands.api.localization.context.replyLocalizedEphemeral
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

// This data class is practically pointless;
// this is just to demonstrate how you can group parameters together,
// so you can benefit from functions/backed properties limited to your parameters,
// without polluting classes with extensions
data class DeleteTimeframe(val time: Long, val unit: TimeUnit) {
    override fun toString(): String = "$time ${unit.name.lowercase()}"
}

@BService
class SlashBan(private val componentsService: Components, private val banService: BanService) {
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @LocalizationBundle("Commands", prefix = "ban") localizationContext: AppLocalizationContext,
        target: InputUser,
        timeframe: DeleteTimeframe,
        reason: String = localizationContext.localize("outputs.defaultReason")
    ) {
        target.member?.let { targetMember ->
            if (!event.member.canInteract(targetMember)) {
                return event.replyLocalizedEphemeral(localizationContext, "errors.user.interactError", "userMention" to target.asMention).queue()
            } else if (!event.guild.selfMember.canInteract(targetMember)) {
                return event.replyLocalizedEphemeral(localizationContext, "errors.bot.interactError", "userMention" to target.asMention).queue()
            }
        }

        val cancelButton = componentsService.ephemeralButton(ButtonStyle.PRIMARY, localizationContext.localize("buttons.cancel")) {
            oneUse = true
            // Restrict button to caller, not necessary since this is an ephemeral reply tho
            constraints += event.user
        }
        val confirmButton = componentsService.ephemeralButton(ButtonStyle.DANGER, localizationContext.localize("buttons.confirm")) {
            oneUse = true
            // Restrict button to caller, not necessary since this is an ephemeral reply tho
            constraints += event.user
        }

        val componentGroup = componentsService.newEphemeralGroup(cancelButton, confirmButton) {
            timeout(1.minutes)
        }

        event.replyLocalizedEphemeral(localizationContext, "outputs.confirmationMessage", "userMention" to target.asMention)
            .addActionRow(cancelButton, confirmButton)
            .queue()

        val componentEvent: ButtonEvent = try {
            componentGroup.awaitAny()
        } catch (e: TimeoutCancellationException) {
            return event.hook.editLocalized(localizationContext, "outputs.timeout")
                .deleteDelayed(event.hook, 5.seconds)
                .queue()
        }

        when (componentEvent.componentId) {
            cancelButton.id -> {
                logger.debug { "Ban cancelled for ${target.id}" }
                componentEvent.replaceLocalized(localizationContext, "outputs.cancelled").queue()

                //Cancel logic
            }
            confirmButton.id -> {
                logger.debug { "Ban confirmed for ${target.id}, $timeframe of messages were deleted, reason: '$reason'" }

                componentEvent.replaceLocalized(
                    localizationContext,
                    "outputs.success",
                    "userMention" to target.asMention,
                    "time" to timeframe.time,
                    "unit" to timeframe.unit.localize(timeframe.time, localizationContext),
                    "reason" to reason
                ).queue()

                //Ban logic
            }
            else -> throw IllegalArgumentException("Unknown button ID: ${componentEvent.componentId}")
        }
    }
}

@Command
@ConditionalService(FrontendChooser::class)
class SlashBanDetailedFront {
    @AppDeclaration
    fun onDeclare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("ban", function = SlashBan::onSlashBan) {
            description = "Ban any user from this guild"

            botPermissions += Permission.BAN_MEMBERS
            userPermissions += Permission.BAN_MEMBERS

            customOption("localizationContext")

            option("target") {
                description = "The user to ban"
            }

            aggregate("timeframe", ::DeleteTimeframe) {
                option("time") {
                    description = "The timeframe of messages to delete with the specified unit"
                }

                option("unit") {
                    description = "The unit of the delete timeframe"

                    usePredefinedChoices = true
                }
            }

            option("reason") {
                description = "The reason for the ban"
            }
        }
    }
}

@Command
@SimpleFrontend
@ConditionalService(FrontendChooser::class)
class SlashBanSimplifiedFront(private val banImpl: SlashBan) : ApplicationCommand() {
    @UserPermissions(Permission.BAN_MEMBERS)
    @BotPermissions(Permission.BAN_MEMBERS)
    @JDASlashCommand(name = "ban", description = "Ban any user from this guild")
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @LocalizationBundle("Commands", prefix = "ban") localizationContext: AppLocalizationContext,
        @SlashOption(description = "The user to ban") target: InputUser,
        @SlashOption(description = "The timeframe of messages to delete with the specified unit") time: Long,
        @SlashOption(description = "The unit of the delete timeframe", usePredefinedChoices = true) unit: TimeUnit,
        @SlashOption(description = "The reason for the ban") reason: String = localizationContext.localize("outputs.defaultReason")
    ) = banImpl.onSlashBan(event, localizationContext, target, DeleteTimeframe(time, unit), reason)
}