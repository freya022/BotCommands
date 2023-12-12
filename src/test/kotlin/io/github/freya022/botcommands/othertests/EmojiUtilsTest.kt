package io.github.freya022.botcommands.othertests

import io.github.freya022.botcommands.api.utils.EmojiUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

object EmojiUtilsTest {
    @Test
    fun `resolve regional indicator aliases`() {
        for (c in 'a'..'z') {
            val unicode = EmojiUtils.resolveEmoji("regional_indicator_$c")
            val expected = indicatorByOffset(c - 'a')
            assertEquals(expected, unicode, "Invalid indicator character $c")
        }
    }

    @Test
    fun `resolve regional indicator unicodes`() {
        for (i in 0 ..< 26) {
            val expected = indicatorByOffset(i)
            val unicode = EmojiUtils.resolveEmoji(expected)
            assertEquals(expected, unicode, "Invalid indicator unicode offset $i");
        }
    }

    private fun indicatorByOffset(offset: Int): String =
        Character.toString("\uD83C\uDDE6".codePointAt(0) + offset)
}