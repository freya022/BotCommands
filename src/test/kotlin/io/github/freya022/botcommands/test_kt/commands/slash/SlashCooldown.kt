package io.github.freya022.botcommands.test_kt.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.builder.cooldown
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds

@Command
class SlashCooldown : ApplicationCommand() {
    @JDASlashCommand(name = "cooldown_annotated")
    @Cooldown(cooldown = 5, unit = ChronoUnit.SECONDS, rateLimitScope = RateLimitScope.GUILD)
    fun onSlashCooldown(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("cooldown", function = ::onSlashCooldown) {
            cooldown(RateLimitScope.GUILD, 5.seconds)
        }
    }
}