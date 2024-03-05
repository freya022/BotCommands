package io.github.freya022.botcommands.test

import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfo
import io.github.freya022.botcommands.test.switches.TestService
import net.dv8tion.jda.api.EmbedBuilder

@BService
@TestService
class MyHelpBuilderConsumer : HelpBuilderConsumer {
    override fun accept(builder: EmbedBuilder, isGlobal: Boolean, commandInfo: TextCommandInfo?) {
        builder.addField("A field name", "Test for ${javaClass.simpleName}", false)
    }
}