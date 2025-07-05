package io.github.freya022.botcommands.test.readme

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.annotations.TextCommandData
import io.github.freya022.botcommands.api.commands.text.annotations.TextOption
import io.github.freya022.botcommands.api.core.utils.deleteDelayed
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

@Command
class TextBan : TextCommand() {
    // Applies to all variations with this path
    @TextCommandData(path = ["ban"], description = "Bans an user")
    // Applies to this variation
    @JDATextCommandVariation(path = ["ban"], description = "Bans the mentioned user")
    suspend fun onTextBan(
        event: BaseCommandEvent,
        @TextOption user: User,
        @TextOption(example = "2") timeframe: Long,
        @TextOption unit: TimeUnit, // A resolver is used here
        @TextOption(example = "Get banned") reason: String = "No reason supplied" // Optional
    ) {
        // ...
        event.reply("${user.asMention} has been banned")
            .deleteDelayed(5.seconds)
            .await()
    }

    @JDATextCommandVariation(path = ["ban"], description = "Bans an user by its name")
    suspend fun onTextBanByName(
        event: BaseCommandEvent,
        @TextOption(example = "freya02") name: String,
        @TextOption(example = "2") timeframe: Long,
        @TextOption unit: TimeUnit, // A resolver is used here
        @TextOption(example = "Get banned") reason: String = "No reason supplied" // Optional
    ) {
        // ...
        event.reply("$name has been banned")
            .deleteDelayed(5.seconds)
            .await()
    }

    // Applies to all variations with this path
    @TextCommandData(path = ["ban", "temp"], description = "Temporarily bans an user")
    // Applies to this variation
    @JDATextCommandVariation(path = ["ban", "temp"], description = "Temporarily bans the mentioned user")
    suspend fun onTextBanTemp(
        event: BaseCommandEvent,
        @TextOption user: User,
        @TextOption(example = "2") timeframe: Long,
        @TextOption unit: TimeUnit, // A resolver is used here
        @TextOption(example = "Get banned") reason: String = "No reason supplied" // Optional
    ) {
        // ...
        event.reply("${user.asMention} has been banned")
            .deleteDelayed(5.seconds)
            .await()
    }

    @JDATextCommandVariation(path = ["ban", "temp"], description = "Temporarily bans an user by name")
    suspend fun onTextBanTempByName(
        event: BaseCommandEvent,
        @TextOption(example = "freya02") name: String,
        @TextOption(example = "2") timeframe: Long,
        @TextOption unit: TimeUnit, // A resolver is used here
        @TextOption(example = "Get banned") reason: String = "No reason supplied" // Optional
    ) {
        // ...
        event.reply("$name has been banned")
            .deleteDelayed(5.seconds)
            .await()
    }
}