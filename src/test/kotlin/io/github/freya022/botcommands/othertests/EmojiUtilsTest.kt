package io.github.freya022.botcommands.othertests

import io.github.freya022.botcommands.api.utils.EmojiUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

object EmojiUtilsTest {
    @Test
    fun `resolve regional indicator aliases`() {
        for (c in 'a'..'z') {
            val unicode = assertDoesNotThrow("Unknown indicator $c") {
                EmojiUtils.resolveEmoji("regional_indicator_$c")
            }
            val expected = indicatorByOffset(c - 'a')
            assertEquals(expected, unicode)
        }
    }

    @Test
    fun `resolve regional indicator unicodes`() {
        for (i in 0 ..< 26) {
            val expected = indicatorByOffset(i)
            val unicode = assertDoesNotThrow("Unknown indicator unicode offset $i") {
                EmojiUtils.resolveEmoji(expected)
            }
            assertEquals(expected, unicode);
        }
    }

    private fun indicatorByOffset(offset: Int): String =
        Character.toString("\uD83C\uDDE6".codePointAt(0) + offset)
}