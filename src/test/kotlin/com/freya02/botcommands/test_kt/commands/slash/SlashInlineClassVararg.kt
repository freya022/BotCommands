package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.VarArgs
import dev.minn.jda.ktx.messages.reply_

@Command
class SlashInlineClassVararg : ApplicationCommand() {
    @JvmInline
    value class MyInlineList(val args: List<String>)

    @JDASlashCommand(name = "inline_class_vararg_annotated")
    fun onSlashAggregate(event: GuildSlashEvent, @AppOption @VarArgs(2, numRequired = 1) inlineList: MyInlineList) {
        event.reply_("$inlineList", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(applicationCommandManager: GlobalApplicationCommandManager) {
        applicationCommandManager.slashCommand("inline_class_vararg", function = ::onSlashAggregate) {
           inlineClassOptionVararg("inlineList", MyInlineList::class.java, 2, 1, { "item_$it" }) {
               description = "Item #$it of inline list"
           }
        }
    }
}