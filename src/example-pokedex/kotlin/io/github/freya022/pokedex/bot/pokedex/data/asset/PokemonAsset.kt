package io.github.freya022.pokedex.bot.pokedex.data.asset

import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.entities.Icon

interface PokemonAsset {

    val name: String

    suspend fun toIcon(): Icon

    suspend fun toThumbnail(): Thumbnail

    suspend fun toMediaGalleryItem(): MediaGalleryItem
}