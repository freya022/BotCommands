package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.into
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.modals.Modals
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu

// Uses breakpoints to see stuff about the interaction
@Command
class SlashEntity(private val buttons: Buttons, private val modals: Modals, private val selectMenus: SelectMenus) : GlobalApplicationCommandProvider {
    data class AllEntities(
        val user: User,
        val member: Member,
        val role: Role,
        val textChannel: TextChannel,
        val newsChannel: NewsChannel,
        val stageChannel: StageChannel,
        val voiceChannel: VoiceChannel,
        val category: Category,
        val forumChannel: ForumChannel,
        val threadChannel: ThreadChannel
//        val mediaChannel: MediaChannel
    )

    suspend fun onSlashEntityNothing(event: GuildSlashEvent) {
        reply(event, null)
    }

    suspend fun onSlashEntityMember(event: GuildSlashEvent, member: Member) {
        reply(event, member)
    }

    suspend fun onSlashEntityRole(event: GuildSlashEvent, role: Role) {
        reply(event, role)
    }

    suspend fun onSlashEntityTextChannel(event: GuildSlashEvent, textChannel: TextChannel) {
        reply(event, textChannel)
    }

    suspend fun onSlashEntityAll(event: GuildSlashEvent, allEntities: AllEntities) {
        reply(event, allEntities)
    }

    private suspend fun reply(event: GuildSlashEvent, arg: Any?) {
        event.deferReply(false).await()

        println(arg)

        val message = MessageCreate {
            content = "It works! <@222046562543468545>"

            components += buttons.primary("Click me").ephemeral {
                bindTo {
                    it.editMessage("Boo").await()
                    it.hook.sendMessage("ye").await()
//                    it.reply_("Ye", ephemeral = true).await()
                }
            }.into()

            components += selectMenus.entitySelectMenu(EntitySelectMenu.SelectTarget.CHANNEL).ephemeral {
                bindTo {
                    it.editMessage("Foo ${it.values}").await()
                    it.hook.sendMessage("bar").await()
                }
            }.into()

            components += selectMenus.entitySelectMenu(EntitySelectMenu.SelectTarget.USER).ephemeral {
                bindTo {
                    it.editMessage("Foo ${it.values}").await()
                    it.hook.sendMessage("bar").await()
                }
            }.into()

            components += selectMenus.entitySelectMenu(EntitySelectMenu.SelectTarget.ROLE).ephemeral {
                bindTo {
                    it.editMessage("Foo ${it.values}").await()
                    it.hook.sendMessage("bar").await()
                }
            }.into()
        }

        event.hook.sendMessage(message).await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("entities", function = null) {
            contexts = enumSetOf(InteractionContextType.GUILD)
            integrationTypes = IntegrationType.ALL

            subcommand("nothing", ::onSlashEntityNothing)
            subcommand("all", ::onSlashEntityAll) {
                aggregate("allEntities", SlashEntity::AllEntities) {
                    option("user")
                    option("member")
                    option("role")
                    option("textChannel")
                    option("newsChannel")
                    option("stageChannel")
                    option("voiceChannel")
                    option("category")
                    option("forumChannel")
                    option("threadChannel")
//                    option("mediaChannel")
                }
            }
            subcommand("member", ::onSlashEntityMember) {
                option("member")
            }
            subcommand("role", ::onSlashEntityRole) {
                option("role")
            }
            subcommand("text_channel", ::onSlashEntityTextChannel) {
                option("textChannel")
            }
        }
    }
}