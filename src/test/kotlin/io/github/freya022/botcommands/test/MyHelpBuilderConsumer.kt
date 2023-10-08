package io.github.freya022.botcommands.test

import io.github.freya022.botcommands.api.commands.prefixed.HelpBuilderConsumer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandInfo
import net.dv8tion.jda.api.EmbedBuilder

@BService
object MyHelpBuilderConsumer : HelpBuilderConsumer {
    override fun accept(builder: EmbedBuilder, isGlobal: Boolean, commandInfo: TextCommandInfo?) {
        builder.addField("A field name", "Test for ${javaClass.simpleName}", false)
    }
}