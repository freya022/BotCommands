package io.github.freya022.pokedex.bot.pokedex.data.asset

import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.utils.FileUpload
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readBytes

class LocalPokemonAsset(private val path: Path) : PokemonAsset {

    override val name: String get() = path.name

    override suspend fun toIcon(): Icon {
        return Icon.from(path.readBytes())
    }

    override suspend fun toThumbnail(): Thumbnail {
        return Thumbnail.fromFile(FileUpload.fromData(path))
    }

    override suspend fun toMediaGalleryItem(): MediaGalleryItem {
        return MediaGalleryItem.fromFile(FileUpload.fromData(path))
    }
}