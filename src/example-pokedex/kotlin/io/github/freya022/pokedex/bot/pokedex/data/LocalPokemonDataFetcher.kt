package io.github.freya022.pokedex.bot.pokedex.data

import io.github.freya022.pokedex.bot.pokedex.Pokemon
import io.github.freya022.pokedex.bot.pokedex.data.asset.LocalPokemonAsset
import io.github.freya022.pokedex.bot.pokedex.data.asset.PokemonAsset
import java.nio.file.Path
import kotlin.io.path.*

private val galleryRules = mapOf(
    "Nidoran♀" to "Nidorina",
    "Nidoran♂" to "Nidorino",
    "Farfetch'd" to "Farfetchd",
    "Mr. Mime" to "MrMime",
)

class LocalPokemonDataFetcher : PokemonDataFetcher {

    private val rootDirectory: Path = Path("pokemon-data")
    private val emojisDirectory: Path = rootDirectory.resolve("images/emojis")
    private val galleryDirectory: Path = rootDirectory.resolve("images/media-gallery")
    private val imagesDirectory = rootDirectory.resolve("images")
    private val thumbnailsDirectory = imagesDirectory.resolve("thumbnails")

    init {
        check(rootDirectory.exists()) {
            "Pokemon data is absent, it must be at: ${rootDirectory.absolutePathString()}"
        }
    }

    override suspend fun getPokedexData(): ByteArray {
        return rootDirectory.resolve("pokedex.json").readBytes()
    }

    override suspend fun getPokemonEmojis(pokemonId: Int): List<PokemonAsset> {
        val currentPokemonEmojisDirectory = emojisDirectory.resolve("$pokemonId")
        return (0..<4)
            .map { index -> currentPokemonEmojisDirectory.resolve("${pokemonId}_part_${index}.png") }
            .map { LocalPokemonAsset(it) }
    }

    override suspend fun getPokedexHeader(): PokemonAsset {
        return imagesDirectory
            .resolve("pokedex-header.webp")
            .let(::LocalPokemonAsset)
    }

    override suspend fun getPokemonThumbnail(pokemonId: Int): PokemonAsset {
        return thumbnailsDirectory
            .resolve("%03d.png".format(pokemonId))
            .let(::LocalPokemonAsset)
    }

    @OptIn(ExperimentalPathApi::class)
    override suspend fun getRandomImages(pokemon: Pokemon, n: Int): Collection<PokemonAsset> {
        val directoryName = galleryRules[pokemon.name.english] ?: pokemon.name.english
        val pokemonGalleryDirectory = galleryDirectory.resolve(directoryName)
        require(pokemonGalleryDirectory.exists()) {
            "Directory $pokemonGalleryDirectory does not exist!"
        }

        return pokemonGalleryDirectory.walk()
            .filter { it.extension == "avif" }
            .shuffled()
            .take(n)
            .map { LocalPokemonAsset(it) }
            .toList()
    }
}