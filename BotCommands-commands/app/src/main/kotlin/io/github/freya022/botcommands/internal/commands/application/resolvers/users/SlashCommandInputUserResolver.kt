package io.github.freya022.botcommands.internal.commands.application.resolvers.users

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.entities.inputUser
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.users.AbstractInputUserResolver
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
internal class SlashCommandInputUserResolver :
        AbstractInputUserResolver<SlashCommandInputUserResolver>(),
        SlashParameterResolver<SlashCommandInputUserResolver, InputUser> {

    override val optionType: OptionType = OptionType.USER

    override fun resolve(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): InputUser {
        return event.inputUser
    }
}
