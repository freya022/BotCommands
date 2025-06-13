package io.github.freya022.pokedex.bot.pokedex.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.minn.jda.ktx.util.await
import io.github.freya022.pokedex.bot.pokedex.Pokemon
import io.github.freya022.pokedex.bot.pokedex.data.asset.GitHubPokemonAsset
import io.github.freya022.pokedex.bot.pokedex.data.asset.PokemonAsset
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.util.concurrent.ConcurrentHashMap

private val galleryRules = mapOf(
    "Nidoran♀" to "Nidorina",
    "Nidoran♂" to "Nidorino",
    "Farfetch'd" to "Farfetchd",
    "Mr. Mime" to "MrMime",
)

private const val baseRawUrl = "https://raw.githubusercontent.com/DV8FromTheWorld/discord-pokedex/refs/heads/main/pokemon-data"
private const val baseContentsApiUrl = "https://api.github.com/repos/DV8FromTheWorld/discord-pokedex/contents/pokemon-data"

class GitHubPokemonDataFetcher : PokemonDataFetcher {

    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
        .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    private val mediaGalleryCache: MutableMap<String, List<PokemonAsset>> = hashMapOf()
    private val mediaGalleryCacheLocks: MutableMap<String, Mutex> = ConcurrentHashMap()
    private val mediaGalleryCacheLocksLock = Mutex()

    override suspend fun getPokedexData(): ByteArray {
        return Request.Builder()
            .url("$baseRawUrl/pokedex.json")
            .build()
            .fetchBody(this).bytes()
    }

    override suspend fun getPokemonEmojis(pokemonId: Int): List<PokemonAsset> {
        return (0..<4).map { index -> GitHubPokemonAsset(this, "$baseRawUrl/images/emojis/${pokemonId}/${pokemonId}_part_${index}.png") }
    }

    override suspend fun getPokedexHeader(): PokemonAsset {
        return GitHubPokemonAsset(this, "$baseRawUrl/images/pokedex-header.webp")
    }

    override suspend fun getPokemonThumbnail(pokemonId: Int): PokemonAsset {
        return GitHubPokemonAsset(this, "$baseRawUrl/images/thumbnails/%03d.png".format(pokemonId))
    }

    override suspend fun getRandomImages(pokemon: Pokemon, n: Int): Collection<PokemonAsset> {
        val images = mediaGalleryCacheLocksLock.withLock {
            val pokemonName = pokemon.name.english
            mediaGalleryCacheLocks.getOrPut(pokemonName) { Mutex() }.withLock {
                mediaGalleryCache.getOrPut(pokemonName) {
                    val directoryName = galleryRules[pokemonName] ?: pokemonName

                    Request.Builder()
                        .url("$baseContentsApiUrl/images/media-gallery/${directoryName}")
                        .build()
                        .fetchBody(this).string()
                        .let { mapper.readValue<List<File>>(it) }
                        .map { GitHubPokemonAsset(this, it.downloadUrl) }
                }
            }
        }

        return images.asSequence()
            .shuffled()
            .take(n)
            .toList()
    }

    companion object {
        suspend fun Request.fetchBody(fetcher: GitHubPokemonDataFetcher): ResponseBody {
            val response = fetcher.client.newCall(this).await()
            check(response.isSuccessful) {
                "Could not fetch pokemon asset from '$url': ${response.code} ${response.message}"
            }

            return response.body!!
        }
    }

    class File(
        @JsonProperty("download_url")
        val downloadUrl: String,
    )
}