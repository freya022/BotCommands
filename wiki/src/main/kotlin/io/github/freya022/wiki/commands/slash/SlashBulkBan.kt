package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.MentionsString
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.utils.awaitCatching
import io.github.freya022.botcommands.api.core.utils.awaitUnit
import io.github.freya022.botcommands.api.core.utils.handle
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toDuration
import kotlin.time.toDurationUnit
import kotlin.time.toJavaDuration

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:bulk_ban-kotlin]
@Command
class SlashBulkBan : ApplicationCommand() {
    @JDASlashCommand(name = "bulk_ban", description = "Ban users in bulk")
    suspend fun onSlashBulkBan(
        event: GuildSlashEvent,
        @SlashOption(description = "Users to ban") @MentionsString users: List<InputUser>,
        @SlashOption(description = "Time frame of messages to delete") timeframe: Long,
        @SlashOption(description = "Unit of the time frame", usePredefinedChoices = true) unit: TimeUnit,
    ) {
        // Check if any member cannot be banned
        val higherMembers = users.mapNotNull { it.member }.filterNot { event.guild.selfMember.canInteract(it) }
        if (higherMembers.isNotEmpty()) {
            return event.reply_("Cannot ban ${higherMembers.joinToString { it.asMention }} as they have equal/higher roles", ephemeral = true).awaitUnit()
        }

        event.deferReply(true).queue()

        event.guild.ban(users, timeframe.toDuration(unit.toDurationUnit())).awaitCatching()
            // Make sure to use onSuccess first,
            // as 'handle' will clear the result type
            .onSuccess {
                event.hook.send("Banned ${it.bannedUsers.size} users, ${it.failedUsers.size} failed").await()
            }
            .handle(ErrorResponse.MISSING_PERMISSIONS) {
                event.hook.send("Could not ban users due to missing permissions").await()
            }
            .handle(ErrorResponse.FAILED_TO_BAN_USERS) {
                event.hook.send("Could not ban anyone").await()
            }
            // Throw unhandled exceptions
            .getOrThrow()
    }
}
// --8<-- [end:bulk_ban-kotlin]

private fun Guild.ban(users: Collection<UserSnowflake>, duration: Duration) =
    ban(users, duration.toJavaDuration())