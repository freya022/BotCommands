package com.freya02.botcommands.test.commands_kt

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.application.annotations.GeneratedOption
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KClass
import kotlin.reflect.KType

@CommandMarker
class NewSlashTest : ApplicationCommand() {
    override fun getDefaultValueSupplier(
        context: BContext, guild: Guild,
        commandId: String?, commandPath: CommandPath,
        optionName: String, parameterType: KType, erasedType: KClass<*>
    ): DefaultValueSupplier? {
        if (commandPath.fullPath == "test") {
            if (optionName == "guild_name") {
                return DefaultValueSupplier { guild.name }
            }
        }

        return super.getDefaultValueSupplier(context, guild, commandId, commandPath, optionName, parameterType, erasedType)
    }

    @JDASlashCommand(name = "test", scope = CommandScope.GUILD)
    fun onSlashTest(event: GuildSlashEvent, @GeneratedOption guildName: String) {
        event.reply_("woo in $guildName", ephemeral = true).queue()
    }
}