package io.github.freya022.botcommands.internal.emojis

import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer
import io.github.freya022.botcommands.internal.core.ClassPathProcessor

internal object AppEmojiContainerProcessor : ClassPathProcessor {

    internal val emojiClasses = arrayListOf<AppEmojiContainerData>()

    override fun processClass(data: ClassPathProcessor.ClassData) {
        val kClass = data.kClass
        kClass.findAnnotationRecursive<AppEmojiContainer>()?.let {
            emojiClasses += AppEmojiContainerData(kClass, it)
        }
    }

    internal fun clear() {
        emojiClasses.clear()
    }
}
