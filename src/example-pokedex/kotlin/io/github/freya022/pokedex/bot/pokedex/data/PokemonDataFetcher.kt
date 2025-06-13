package io.github.freya022.pokedex.bot.pokedex.data

import io.github.freya022.pokedex.bot.pokedex.Pokemon
import io.github.freya022.pokedex.bot.pokedex.data.asset.PokemonAsset

interface PokemonDataFetcher {

    suspend fun getPokedexData(): ByteArray

    suspend fun getPokemonEmojis(pokemonId: Int): List<PokemonAsset>

    suspend fun getPokedexHeader(): PokemonAsset

    suspend fun getPokemonThumbnail(pokemonId: Int): PokemonAsset

    suspend fun getRandomImages(pokemon: Pokemon, n: Int): Collection<PokemonAsset>
}

suspend fun Pokemon.getThumbnail(strategy: PokemonDataFetcher) = strategy.getPokemonThumbnail(id)

suspend fun Pokemon.getRandomImages(strategy: PokemonDataFetcher, n: Int) = strategy.getRandomImages(this, n)