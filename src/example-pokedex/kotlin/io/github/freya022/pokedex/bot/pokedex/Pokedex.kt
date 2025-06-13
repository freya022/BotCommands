package io.github.freya022.pokedex.bot.pokedex

import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.pokedex.bot.pokedex.data.PokemonDataFetcher
import kotlinx.coroutines.runBlocking

private const val ENTRIES_PER_PAGE = 10

@BService
class Pokedex(
    pokemonDataFetcher: PokemonDataFetcher,
) {

    val pokemons: Map<Int, Pokemon>
    val maxPage: Int

    init {
        val mapper = jacksonObjectMapper()
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        val allPokemons = runBlocking { mapper.readTree(pokemonDataFetcher.getPokedexData()) }

        val first151PokemonsJson = allPokemons.takeWhile { it["id"].asInt() <= 151 }
        val first151Pokemons = mapper.treeToValue<List<Pokemon>>(ArrayNode(mapper.nodeFactory, first151PokemonsJson))

        pokemons = first151Pokemons.associateBy { it.id }
        maxPage = pokemons.size.floorDiv(ENTRIES_PER_PAGE) // No +1 as this is 0-based
    }

    fun getById(id: Int): Pokemon = pokemons[id] ?: error("No pokemon with id $id")
    fun getByIdOrNull(id: Int): Pokemon? = pokemons[id]

    fun getPage(page: Int): List<Pokemon> {
        val offset = page * ENTRIES_PER_PAGE
        val range = offset..<offset + ENTRIES_PER_PAGE

        return range.mapNotNull { pokemons[it] }
    }
}