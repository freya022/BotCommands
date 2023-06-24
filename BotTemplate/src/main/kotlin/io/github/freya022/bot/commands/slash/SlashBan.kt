package io.github.freya022.bot.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.localization.Localization.Entry.entry
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.TimeoutCancellationException
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger { }

@Command
class SlashBan(private val componentsService: Components) {
    // This data class is practically pointless,
    // this is just to demonstrate how you can group parameters together,
    // so you can benefit from functions/properties limited to your parameters,
    // without polluting classes with extensions
    data class DeleteTimeframe(val time: Long, val unit: TimeUnit)

    @CommandMarker //So IJ doesn't tell us to make it private
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @LocalizationBundle("Commands", prefix = "ban") localizationContext: AppLocalizationContext,
        target: User,
        timeframe: DeleteTimeframe,
        reason: String = localizationContext.localize("outputs.defaultReason")
    ) {
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
            componentGroup.await()
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
                    entry("unit", timeframe.unit.name.lowercase().trimEnd('s')),
                    entry("reason", reason)
                )).setComponents().queue()

                //Ban logic
            }
            else -> throw IllegalArgumentException("Unknown button ID: ${componentEvent.componentId}")
        }
    }

    @AppDeclaration
    fun onDeclare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("ban", function = ::onSlashBan) {
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