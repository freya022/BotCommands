package io.github.freya022.botcommands.internal.commands.application.resolvers.enumerations

import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumNameFunction
import io.github.freya022.botcommands.api.commands.application.resolvers.enumerations.SlashCommandEnumResolver.ValuesSupplier
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.lang.Enum as JavaEnum

internal class SlashCommandEnumResolverImpl<E : Enum<E>> internal constructor(
    private val enumType: Class<E>,
    private val valuesSupplier: ValuesSupplier<E>,
    private val nameFunction: EnumNameFunction<E>,
) : ClassParameterResolver<SlashCommandEnumResolverImpl<E>, E>(enumType),
    SlashParameterResolver<SlashCommandEnumResolverImpl<E>, E> {

    override val optionType: OptionType = OptionType.STRING

    override fun getPredefinedChoices(guild: Guild?): Collection<Command.Choice> {
        return valuesSupplier.get(guild).map { Command.Choice(nameFunction.apply(it), it.name) }
    }

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): E = JavaEnum.valueOf<E>(enumType, optionMapping.asString)
}
