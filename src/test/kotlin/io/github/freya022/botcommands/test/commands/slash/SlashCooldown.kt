package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.builder.cooldown
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds

@Command
class SlashCooldown : GlobalApplicationCommandProvider, ApplicationCommand() {
    @JDASlashCommand(name = "cooldown_annotated")
    @Cooldown(cooldown = 5, unit = ChronoUnit.SECONDS, rateLimitScope = RateLimitScope.GUILD)
    fun onSlashCooldown(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("cooldown", function = ::onSlashCooldown) {
            cooldown(5.seconds, RateLimitScope.GUILD)
        }
    }
}