package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.builder.cooldown
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds

@Command
class SlashCooldown : GlobalApplicationCommandProvider {
    @JDASlashCommand(name = "cooldown_annotated")
    @Cooldown(cooldown = 5, unit = ChronoUnit.SECONDS, scope = RateLimitScope.GUILD)
    fun onSlashCooldown(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("cooldown", function = ::onSlashCooldown) {
            cooldown(5.seconds, RateLimitScope.GUILD)
        }
    }
}
