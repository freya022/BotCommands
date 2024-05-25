package doc.kotlin.examples.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu

@Command
class SlashTypeSafeSelectMenus(private val selectMenus: SelectMenus) : ApplicationCommand() {
    @JDASlashCommand(name = "type_safe_select_menus", description = "Demo of Kotlin type-safe bindings")
    suspend fun onSlashTypeSafeSelectMenus(event: GuildSlashEvent, @SlashOption argument: String) {
        val selectMenu = selectMenus.entitySelectMenu(EntitySelectMenu.SelectTarget.ROLE).persistent {
            bindTo(::onTestSelect, argument)
        }

        event.replyComponents(selectMenu.into()).await()
    }

    @JDASelectMenuListener
    suspend fun onTestSelect(event: EntitySelectEvent, @ComponentData argument: String) {
        event.reply_("The argument was: $argument", ephemeral = true).await()
    }
}