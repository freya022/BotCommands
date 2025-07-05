package io.github.freya022.botcommands.internal.emojis

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer
import org.jetbrains.annotations.TestOnly
import kotlin.reflect.KClass

internal object AppEmojiContainerProcessor : ClassGraphProcessor {

    internal val emojiClasses = arrayListOf<AppEmojiContainerData>()

    override fun processClass(
        serviceContainer: ServiceContainer,
        classInfo: ClassInfo,
        kClass: KClass<*>,
        isService: Boolean
    ) {
        kClass.findAnnotationRecursive<AppEmojiContainer>()?.let {
            emojiClasses += AppEmojiContainerData(kClass, it)
        }
    }

    @TestOnly
    internal fun clear() {
        emojiClasses.clear()
    }
}