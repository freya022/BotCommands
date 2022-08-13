package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.CooldownScope
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_
import java.util.concurrent.TimeUnit

@CommandMarker
class SlashCooldown {
    @CommandMarker
    fun onSlashCooldown(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @Declaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("cooldown") {
            cooldown {
                cooldown = 5
                unit = TimeUnit.SECONDS
                scope = CooldownScope.GUILD
            }

            function = ::onSlashCooldown
        }
    }
}