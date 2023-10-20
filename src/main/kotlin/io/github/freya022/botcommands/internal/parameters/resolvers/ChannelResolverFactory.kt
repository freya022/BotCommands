package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.*
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import io.github.freya022.botcommands.internal.components.ComponentDescriptor
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf

interface IChannelResolver {
    val channelTypes: EnumSet<ChannelType>
}

@ResolverFactory
internal object ChannelResolverFactory : ParameterResolverFactory<ChannelResolverFactory.ChannelResolver>(ChannelResolver::class) {
    private val channelPattern = Pattern.compile("(?:<#)?(\\d+)>?")

    internal class ChannelResolver(private val type: Class<out GuildChannel>, override val channelTypes: EnumSet<ChannelType>) :
        ClassParameterResolver<ChannelResolver, GuildChannel>(GuildChannel::class),
        RegexParameterResolver<ChannelResolver, GuildChannel>,
        SlashParameterResolver<ChannelResolver, GuildChannel>,
        ComponentParameterResolver<ChannelResolver, GuildChannel>,
        IChannelResolver {

        //region Text
        override val pattern: Pattern = channelPattern
        override val testExample: String = "<#1234>"

        override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String =
            event.channel.asMention

        //TODO what happens for unknown threads?
        override suspend fun resolveSuspend(
            variation: TextCommandVariation,
            event: MessageReceivedEvent,
            args: Array<String?>
        ): GuildChannel? = event.guild.getChannelById(type, args[0]!!)
        //endregion

        //region Slash
        override val optionType: OptionType = OptionType.CHANNEL

        override suspend fun resolveSuspend(
            info: SlashCommandInfo,
            event: CommandInteractionPayload,
            optionMapping: OptionMapping
        ): GuildChannel? {
            val channel = optionMapping.asChannel
            if (type.isInstance(channel))
                return type.cast(channel)
            return null
        }
        //endregion

        //region Component
        //TODO what happens for unknown threads?
        override suspend fun resolveSuspend(
            descriptor: ComponentDescriptor,
            event: GenericComponentInteractionCreateEvent,
            arg: String
        ): GuildChannel? {
            val guild = event.guild ?: throwInternal(descriptor.function, "Cannot resolve a Channel outside of a Guild")
            return guild.getChannelById(type, arg)
        }
        //endregion
    }

    override val supportedTypesStr: List<String> = listOf("<out GuildChannel>")

    @Suppress("UNCHECKED_CAST")
    override fun isResolvable(parameter: ParameterWrapper): Boolean {
        val erasure = parameter.erasure
        if (!erasure.isSubclassOf(GuildChannel::class)) return false
        erasure as KClass<out GuildChannel>

        // Only empty if the type is a GuildChannel but is not a concrete interface
        return erasure == GuildChannel::class || ChannelType.fromInterface(erasure.java).isNotEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(parameter: ParameterWrapper): ChannelResolver {
        val erasure = parameter.erasure as KClass<out GuildChannel>
        val channelTypes = ChannelType.fromInterface(erasure.java)
        return ChannelResolver(erasure.java, channelTypes)
    }
}