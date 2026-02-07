package dev.freya02.botcommands.jda.keepalive

import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import net.dv8tion.jda.api.EmbedBuilder

@Command
class SlashTest {

    @JDASlashCommand(name = "test", description = "No description")
    fun onSlashTest(event: GuildSlashEvent) {
        event.reply_(ephemeral = true) {
            embed {
                fun addClass(name: String, inline: Boolean) {
                    field(name = "__${name}__ (0) \n${EmbedBuilder.ZERO_WIDTH_SPACE}", inline = inline)
                }

                println("abcdef")
                addClass(name = "Tank", inline = true)
                addClass(name = "Heal", inline = true)
                field()
                addClass(name = "Melee", inline = true)
                addClass(name = "Range", inline = true)
                field()
            }
        }.queue()
    }
}
