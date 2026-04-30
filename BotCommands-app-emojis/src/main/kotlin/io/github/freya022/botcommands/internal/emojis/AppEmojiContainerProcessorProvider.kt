package io.github.freya022.botcommands.internal.emojis

import io.github.freya022.botcommands.api.core.config.BAppEmojisConfig
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.getConfigOrNull
import io.github.freya022.botcommands.internal.core.ClassPathProcessor
import io.github.freya022.botcommands.internal.core.ClassPathProcessorProvider

internal class AppEmojiContainerProcessorProvider : ClassPathProcessorProvider {

    override fun getProcessors(config: BConfig): Collection<ClassPathProcessor> {
        if (config.getConfigOrNull<BAppEmojisConfig>() != null) {
            return listOf(AppEmojiContainerProcessor)
        }

        return emptyList()
    }
}
