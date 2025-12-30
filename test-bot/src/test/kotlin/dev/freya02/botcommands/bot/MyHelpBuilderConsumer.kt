package dev.freya02.botcommands.bot

import dev.freya02.botcommands.bot.switches.TestService
import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.EmbedBuilder

@BService
@TestService
class MyHelpBuilderConsumer : HelpBuilderConsumer {
    override fun acceptGlobal(builder: EmbedBuilder) {
        builder.addField("A field name", "Test for ${javaClass.simpleName}", false)
    }

    override fun acceptCommand(builder: EmbedBuilder, commandInfo: TextCommandInfo) {
        builder.addField("A field name", "Test for ${javaClass.simpleName}", false)
    }
}
