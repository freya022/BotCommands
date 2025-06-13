package io.github.freya022.pokedex.bot.pokedex.data.asset

import io.github.freya022.pokedex.bot.pokedex.data.GitHubPokemonDataFetcher
import io.github.freya022.pokedex.bot.pokedex.data.GitHubPokemonDataFetcher.Companion.fetchBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.entities.Icon
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

class GitHubPokemonAsset(private val fetcher: GitHubPokemonDataFetcher, private val url: String) : PokemonAsset {

    override val name: String
        get() = url.toHttpUrl().pathSegments.last()

    override suspend fun toIcon(): Icon = withContext(Dispatchers.IO) {
        Icon.from(Request.Builder().url(url).build().fetchBody(fetcher).bytes())
    }

    override suspend fun toThumbnail(): Thumbnail =
        Thumbnail.fromUrl(url)

    override suspend fun toMediaGalleryItem(): MediaGalleryItem =
        MediaGalleryItem.fromUrl(url)
}