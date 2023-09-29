package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.builder.cooldown
import dev.minn.jda.ktx.messages.reply_
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