package io.github.freya022.botcommands.internal.emojis

import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer
import kotlin.reflect.KClass

internal class AppEmojiContainerData(
    val clazz: KClass<*>,
    val annotation: AppEmojiContainer
)