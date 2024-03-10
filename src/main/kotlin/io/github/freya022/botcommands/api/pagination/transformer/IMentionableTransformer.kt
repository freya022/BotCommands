package io.github.freya022.botcommands.api.pagination.transformer

import net.dv8tion.jda.api.entities.IMentionable

class IMentionableTransformer : EntryTransformer<IMentionable> {
    override fun toString(entry: IMentionable): String = entry.asMention
}
