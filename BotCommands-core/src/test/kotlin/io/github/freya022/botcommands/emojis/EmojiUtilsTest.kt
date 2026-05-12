package io.github.freya022.botcommands.emojis

import io.github.freya022.botcommands.api.utils.EmojiUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

object EmojiUtilsTest {
    @Test
    fun `Can get emoji by alias`() {
        assertEquals("\uD83C\uDDE6", EmojiUtils.resolveEmoji("regional_indicator_a"))
    }

    @Test
    fun `Can get emoji by surrogates`() {
        assertEquals("\uD83C\uDDE6", EmojiUtils.resolveEmoji("\uD83C\uDDE6"))
        assertNull(EmojiUtils.resolveEmojiOrNull("\uFE0F"))
    }
}
