package io.github.freya022.pokedex.bot.pokedex

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.walk

private val galleryRules = mapOf(
    "Nidoran♀" to "Nidorina",
    "Nidoran♂" to "Nidorino",
    "Farfetch'd" to "Farfetchd",
    "Mr. Mime" to "MrMime",
)

class Pokemon(
    val id: Int,
    val name: Name,
    val type: List<String>,
    val species: String,
    val description: String,
    val evolution: Evolution,
) {

    @OptIn(ExperimentalPathApi::class)
    val imagePaths: List<Path> = run {
        val directoryName = galleryRules.getOrElse(name.english) { name.english }
        val pokemonGalleryDirectory = Pokedex.galleryDirectory.resolve(directoryName)
        require(pokemonGalleryDirectory.exists()) {
            "Directory $pokemonGalleryDirectory does not exist!"
        }

        pokemonGalleryDirectory.walk().filter { it.extension == "avif" }.toList()
    }

    class Name(
        val english: String,
    )

    class Evolution(
        prev: List<String>?,
        next: List<List<String>>?,
    ) {

        val prev: EvolutionCriteria? = prev?.let(::EvolutionCriteria)
        val next: List<EvolutionCriteria>? = next?.map(::EvolutionCriteria)

        class EvolutionCriteria(
            val id: Int,
            val criteria: List<String>,
        ) {

            constructor(data: List<String>) : this(
                data[0].toInt(),
                data.drop(1),
            )
        }
    }
}

val Pokemon.emojiPaths: List<Path>
    get() {
        val currentPokemonEmojisDirectory = Pokedex.emojisDirectory.resolve("$id")
        return (0..<4).map { index -> currentPokemonEmojisDirectory.resolve("${id}_part_${index}.png") }
    }

val Pokemon.thumbnailUrl: String
    get() = "https://raw.githubusercontent.com/Purukitto/pokemon-data.json/master/images/pokedex/thumbnails/%03d.png".format(id)

fun Pokemon.getRandomImages(n: Int): Set<Path> {
    val images: MutableSet<Path> = hashSetOf()
    while (images.size < n) {
        images.add(imagePaths.random())
    }
    return images
}