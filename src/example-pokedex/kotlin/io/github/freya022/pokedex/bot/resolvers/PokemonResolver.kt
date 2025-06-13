package io.github.freya022.pokedex.bot.resolvers

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.pokedex.bot.pokedex.Pokedex
import io.github.freya022.pokedex.bot.pokedex.Pokemon
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
class PokemonResolver : ClassParameterResolver<PokemonResolver, Pokemon>(Pokemon::class),
                        SlashParameterResolver<PokemonResolver, Pokemon> {

    override val optionType: OptionType = OptionType.INTEGER

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Pokemon? {
        val pokemonId = optionMapping.asInt
        val pokemon = Pokedex.getByIdOrNull(pokemonId)
        if (pokemon == null && event is IReplyCallback) {
            event.reply_("Pokemon #$pokemonId does not exist.", ephemeral = true).queue()
        }

        return pokemon
    }
}