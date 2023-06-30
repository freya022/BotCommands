package io.github.freya022.bot.commands.slash

import com.freya02.botcommands.api.commands.annotations.BotPermissions
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.UserPermissions
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.awaitAny
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import com.freya02.botcommands.api.localization.Localization.Entry.entry
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.bot.commands.FrontendChooser
import io.github.freya022.bot.commands.SimpleFrontend
import io.github.freya022.bot.resolvers.localize
import kotlinx.coroutines.TimeoutCancellationException
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger { }

// This data class is practically pointless;
// this is just to demonstrate how you can group parameters together,
// so you can benefit from functions/backed properties limited to your parameters,
// without polluting classes with extensions
data class DeleteTimeframe(val time: Long, val unit: TimeUnit) {
    override fun toString(): String = "$time ${unit.name.lowercase()}"
}

@BService
class SlashBan(private val componentsService: Components) {
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @LocalizationBundle("Commands", prefix = "ban") localizationContext: AppLocalizationContext,
        target: User,
        timeframe: DeleteTimeframe,
        reason: String = localizationContext.localize("outputs.defaultReason")
    ) {
        val targetMember: Member? = try {
            event.guild.retrieveMember(target).await()
        } catch (e: ErrorResponseException) {
            if (e.errorResponse != ErrorResponse.UNKNOWN_MEMBER)
                throw e
            null
        }

        if (targetMember != null) {
            if (!event.member.canInteract(targetMember)) {
                return event.reply_(localizationContext.localize("errors.user.interactError", entry("userMention", target.asMention)), ephemeral = true).queue()
            } else if (!event.guild.selfMember.canInteract(targetMember)) {
                return event.reply_(localizationContext.localize("errors.bot.interactError", entry("userMention", target.asMention)), ephemeral = true).queue()
            }
        }

        val cancelButton = componentsService.ephemeralButton(ButtonStyle.PRIMARY, localizationContext.localize("buttons.cancel")) {
            // Restrict button to caller, not necessary since this is an ephemeral reply tho
            constraints += event.user
        }
        val confirmButton = componentsService.ephemeralButton(ButtonStyle.DANGER, localizationContext.localize("buttons.confirm")) {
            // Restrict button to caller, not necessary since this is an ephemeral reply tho
            constraints += event.user
        }

        val componentGroup = componentsService.newEphemeralGroup(cancelButton, confirmButton) {
            timeout(1.minutes)
        }

        event.reply_(localizationContext.localize("outputs.confirmationMessage", entry("userMention", target.asMention)), ephemeral = true)
            .addActionRow(cancelButton, confirmButton)
            .queue()

        val componentEvent: ButtonEvent = try {
            componentGroup.awaitAny()
        } catch (e: TimeoutCancellationException) {
            return event.hook.editOriginal(localizationContext.localize("outputs.timeout"))
                .delay(5.seconds.toJavaDuration())
                .flatMap { event.hook.deleteOriginal() }
                .queue()
        }

        when (componentEvent.componentId) {
            cancelButton.id -> {
                logger.debug { "Ban cancelled for ${target.id}" }
                componentEvent.editMessage(localizationContext.localize("outputs.cancelled"))
                    .setComponents()
                    .queue()

                //Cancel logic
            }
            confirmButton.id -> {
                logger.debug { "Ban confirmed for ${target.id}, $timeframe of messages were deleted, reason: '$reason'" }

                componentEvent.editMessage(localizationContext.localize(
                    "outputs.success",
                    entry("userMention", target.asMention),
                    entry("time", timeframe.time),
                    entry("unit", timeframe.unit.localize(timeframe.time, localizationContext)),
                    entry("reason", reason)
                )).setComponents().queue()

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

            botPermissions = EnumSet.of(Permission.BAN_MEMBERS)
            userPermissions = EnumSet.of(Permission.BAN_MEMBERS)

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
        @AppOption(description = "The user to ban") target: User,
        @AppOption(description = "The timeframe of messages to delete with the specified unit") time: Long,
        @AppOption(description = "The unit of the delete timeframe", usePredefinedChoices = true) unit: TimeUnit,
        @AppOption(description = "The reason for the ban") reason: String = localizationContext.localize("outputs.defaultReason")
    ) = banImpl.onSlashBan(event, localizationContext, target, DeleteTimeframe(time, unit), reason)
}