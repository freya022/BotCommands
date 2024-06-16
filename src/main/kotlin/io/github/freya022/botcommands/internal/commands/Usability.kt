package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.commands.Usability.UnusableReason.*
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import java.util.*

class Usability private constructor(val unusableReasons: EnumSet<UnusableReason>) {
    /**
     * @return `true` if the command is **not** executable
     */
    val isUnusable: Boolean by lazy {
        unusableReasons.any { !it.isUsable }
    }

    /**
     * @return `true` if the command is executable
     */
    val isUsable: Boolean
        get() = !isUnusable

    /**
     * @return `true` if the command is **not** showable (in help command for example)
     */
    val isNotShowable: Boolean by lazy {
        unusableReasons.any { !it.isShowable }
    }

    /**
     * @return `true` if the command is showable (in help command for example)
     */
    val isShowable: Boolean
        get() = !isNotShowable

    enum class UnusableReason(
        /**
         * @return `true` if the command is showable (in help command for example)
         */
        val isShowable: Boolean,
        /**
         * @return `true` if the command is executable
         */
        val isUsable: Boolean
    ) {
        HIDDEN(false, false),
        OWNER_ONLY(false, false),
        USER_PERMISSIONS(false, false),
        BOT_PERMISSIONS(true, false),
        NSFW_ONLY(false, false)
    }

    companion object {
        private fun checkNSFW(
            context: BContext,
            unusableReasons: EnumSet<UnusableReason>,
            channel: GuildMessageChannel,
            cmdInfo: TextCommandInfo
        ) {
            // Do not run if command is not NSFW
            if (!cmdInfo.nsfw) return

            if (channel is ThreadChannel) {
               checkNSFW(context, unusableReasons, channel.parentMessageChannel, cmdInfo)
               return
            }

            if (channel is StandardGuildMessageChannel) {
                if (!channel.isNSFW) {
                    unusableReasons.add(NSFW_ONLY)
                }
            } else {
                throwInternal("Unsupported channel: $channel")
            }
        }

        @JvmStatic
        fun of(context: BContext, cmdInfo: TextCommandInfo, member: Member, channel: GuildMessageChannel, isNotOwner: Boolean): Usability {
            enumSetOf<UnusableReason>().apply {
                if (isNotOwner && cmdInfo.hidden) add(HIDDEN)
                if (isNotOwner && cmdInfo.isOwnerRequired) add(OWNER_ONLY)

                checkNSFW(context, this, channel, cmdInfo)

                if (isNotOwner && !member.hasPermission(channel, cmdInfo.userPermissions)) add(USER_PERMISSIONS)
                if (!channel.guild.selfMember.hasPermission(channel, cmdInfo.botPermissions)) add(BOT_PERMISSIONS)

                return Usability(this)
            }
        }

        @JvmStatic
        fun of(event: GenericCommandInteractionEvent, cmdInfo: ApplicationCommandInfo, isNotOwner: Boolean): Usability {
            enumSetOf<UnusableReason>().apply {
                if (!event.isFromGuild) return Usability(this)

                val channel = event.guildChannel
                val guild = event.guild ?: throwInternal("Guild shouldn't be null as this code path is guild-only")
                val member = event.member ?: throwInternal("Member shouldn't be null as this code path is guild-only")

                if (!guild.selfMember.hasPermission(channel, cmdInfo.botPermissions)) add(BOT_PERMISSIONS)
                if (isNotOwner && !member.hasPermission(channel, cmdInfo.userPermissions)) add(USER_PERMISSIONS)

                return Usability(this)
            }
        }
    }
}