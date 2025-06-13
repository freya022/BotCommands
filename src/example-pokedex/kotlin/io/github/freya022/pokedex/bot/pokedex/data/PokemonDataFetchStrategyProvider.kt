package io.github.freya022.pokedex.bot.pokedex.data

import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration
import io.github.freya022.botcommands.api.core.service.annotations.BService

@BConfiguration
object PokemonDataFetchStrategyProvider {

    @BService
    fun pokemonDataFetchStrategy(): PokemonDataFetcher {
        return GitHubPokemonDataFetcher()
    }
}