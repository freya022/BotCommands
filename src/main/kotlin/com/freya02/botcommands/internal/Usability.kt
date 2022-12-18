package com.freya02.botcommands.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.Usability.UnusableReason.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
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
        NSFW_DISABLED(false, false),
        NSFW_ONLY(false, false),
        NSFW_DM_DENIED(false, false);
    }

    companion object {
        private fun checkNSFW(
            context: BContext,
            unusableReasons: EnumSet<UnusableReason>,
            channel: GuildMessageChannel,
            cmdInfo: TextCommandInfo
        ) {
            val nsfwStrategy = cmdInfo.nsfwStrategy ?: return

            //The command is indeed marked NSFW, but where is the command ran ?
            if (channel is ThreadChannel) {
               checkNSFW(context, unusableReasons, channel.parentMessageChannel, cmdInfo)
               return
            }

            if (channel is StandardGuildMessageChannel) {
                //If guild NSFW is not enabled, and we are in a guild channel
                if (!nsfwStrategy.allowedInGuilds) {
                    unusableReasons.add(NSFW_DISABLED)
                } else if (!channel.isNSFW) { //If we are in a non-nsfw channel
                    unusableReasons.add(NSFW_ONLY)
                }
            } else if (channel is PrivateChannel) {
                if (!nsfwStrategy.allowInDMs) {
                    unusableReasons.add(NSFW_DISABLED)
                } else {
                    val provider = context.settingsProvider

                    //If provider is null then assume there is no consent
                    if (provider == null) {
                        unusableReasons.add(NSFW_DM_DENIED)

                        //If the user does not consent
                    } else if (!provider.doesUserConsentNSFW(channel.user!!)) {
                        unusableReasons.add(NSFW_DM_DENIED)
                    }
                }
            }
        }

        @JvmStatic
        fun of(context: BContext, cmdInfo: TextCommandInfo, member: Member, channel: GuildMessageChannel, isNotOwner: Boolean): Usability {
            with(EnumSet.noneOf(UnusableReason::class.java)) {
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
            with(EnumSet.noneOf(UnusableReason::class.java)) {
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