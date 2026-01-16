package dev.freya02.botcommands.bot.readme

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.deleteDelayed
import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

@Command
class SlashBan {
    @JDASlashCommand(name = "ban", description = "Bans an user")
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @SlashOption(description = "The user to ban") user: User,
        @SlashOption(description = "Timeframe of messages to delete") timeframe: Long,
        // Use choices that come from the TimeUnit resolver
        @SlashOption(description = "Unit of the timeframe", usePredefinedChoices = true) unit: TimeUnit, // A resolver is used here
        @SlashOption(description = "Why the user gets banned") reason: String = "No reason supplied" // Optional
    ) {
        // ...
        event.reply_("${user.asMention} has been banned for '$reason'", ephemeral = true)
            .deleteDelayed(5.seconds)
            .await()
    }
}
