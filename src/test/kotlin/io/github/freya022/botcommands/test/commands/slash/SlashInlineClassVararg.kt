package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption

@Command
class SlashInlineClassVararg : ApplicationCommand() {
    @JvmInline
    value class MyInlineList(val args: List<String>)

    @JDASlashCommand(name = "inline_class_vararg_annotated")
    fun onSlashAggregate(event: GuildSlashEvent, @SlashOption @VarArgs(2, numRequired = 1) inlineList: MyInlineList) {
        event.reply_("$inlineList", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(applicationCommandManager: GlobalApplicationCommandManager) {
        applicationCommandManager.slashCommand("inline_class_vararg", function = ::onSlashAggregate) {
           inlineClassOptionVararg<MyInlineList>("inlineList", 2, 1, { "item_$it" }) {
               description = "Item #$it of inline list"
           }
        }
    }
}