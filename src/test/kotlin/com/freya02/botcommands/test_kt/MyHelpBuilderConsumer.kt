package com.freya02.botcommands.test_kt

import com.freya02.botcommands.api.commands.prefixed.HelpBuilderConsumer
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import net.dv8tion.jda.api.EmbedBuilder

@BService
@ServiceType([HelpBuilderConsumer::class])
object MyHelpBuilderConsumer : HelpBuilderConsumer {
    override fun accept(builder: EmbedBuilder, isGlobal: Boolean, commandInfo: TextCommandInfo?) {
        builder.addField("A field name", "Test for ${javaClass.simpleName}", false)
    }
}