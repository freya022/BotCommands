package io.github.freya022.botcommands.test

import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

@BService
@RequiresTextCommands
class MyTextPrefixSupplier(private val textCommandsContext: TextCommandsContext) : TextPrefixSupplier {
    override fun getPrefixes(channel: GuildMessageChannel): List<String> = when {
        channel.idLong == 1274378050154270720L -> emptyList() // No text commands
        else -> textCommandsContext.getDefaultPrefixes()
    }

    override fun getPreferredPrefix(channel: GuildMessageChannel): String {
        return textCommandsContext.getDefaultPrefixes().first()
    }
}