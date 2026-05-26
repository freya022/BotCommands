package io.github.freya022.botcommands.internal.commands.application.resolvers

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
@RequiresApplicationCommands
internal class BooleanResolver : ClassParameterResolver<BooleanResolver, Boolean>(Boolean::class),
                                 SlashParameterResolver<BooleanResolver, Boolean> {

    override val optionType: OptionType get() = OptionType.BOOLEAN

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): Boolean = optionMapping.asBoolean
}
