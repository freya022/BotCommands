package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.Usability.UnusableReason
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandBuilder
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.commands.AbstractCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.UsabilityImpl
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import java.util.*
import java.util.function.Consumer

internal sealed class TextCommandInfoImpl(
    builder: TextCommandBuilder,
    override val parentInstance: TextCommandInfoImpl?
) : AbstractCommandInfoImpl(builder),
    TextCommandInfo {

    final override val subcommands: Map<String, TextCommandInfoImpl>

    final override val variations: List<TextCommandVariationImpl> = builder.variations.map { it.build(this) }.unmodifiableView()

    final override val aliases: List<String> = builder.aliases.toImmutableList()

    final override val description: String? = builder.description

    final override val nsfw: Boolean = builder.nsfw
    final override val isOwnerRequired: Boolean = builder.ownerRequired
    final override val hidden: Boolean = builder.hidden

    final override val detailedDescription: Consumer<EmbedBuilder>? = builder.detailedDescription

    init {
        subcommands = buildMap(builder.subcommands.size + builder.subcommands.sumOf { it.aliases.size }) {
            builder.subcommands.forEach { subcommandBuilder ->
                val textCommandInfo = subcommandBuilder.build(this@TextCommandInfoImpl)
                (subcommandBuilder.aliases + subcommandBuilder.name).forEach { subcommandName ->
                    putIfAbsentOrThrow(subcommandName, textCommandInfo) {
                        "Text subcommand with path '${it.path}' already exists"
                    }
                }
            }
        }
    }

    final override fun getUsability(member: Member, channel: GuildMessageChannel) = UsabilityImpl.build {
        val isNotOwner = !context.isOwner(member.idLong)
        if (isNotOwner && hidden) add(UnusableReason.HIDDEN)
        if (isNotOwner && isOwnerRequired) add(UnusableReason.OWNER_ONLY)

        checkNSFW(channel)

        if (isNotOwner && !member.hasPermission(channel, userPermissions)) add(UnusableReason.USER_PERMISSIONS)
        if (!channel.guild.selfMember.hasPermission(channel, botPermissions)) add(UnusableReason.BOT_PERMISSIONS)
    }

    context(EnumSet<UnusableReason>)
    private fun checkNSFW(channel: GuildMessageChannel) {
        // Do not run if command is not NSFW
        if (!nsfw) return

        if (channel is ThreadChannel) {
            return checkNSFW(channel.parentMessageChannel)
        }

        if (channel is IAgeRestrictedChannel) {
            if (!channel.isNSFW) {
                add(UnusableReason.NSFW_ONLY)
            }
        } else {
            throwInternal("Unsupported channel: $channel")
        }
    }
}