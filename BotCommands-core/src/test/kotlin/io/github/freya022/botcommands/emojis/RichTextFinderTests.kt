package io.github.freya022.botcommands.emojis

import io.github.freya022.botcommands.api.utils.RichTextFinder
import io.github.freya022.botcommands.api.utils.RichTextType
import kotlin.test.Test
import kotlin.test.assertEquals

object RichTextFinderTests {

    @Test
    fun `Extract emoji aliases`() {
        // ":joy: <:joy:abc>" => [joy, <text>, joy, <text>]

        val results = RichTextFinder(
            /* input = */ ":joy: <:joy:abc>",
            /* getIMentionable = */ false,
            /* getGlobalMentions = */ false,
            /* getEmojis = */ true,
            /* getUrls = */ false,
        ).results.toMutableList()

        assertEquals(4, results.size)

        var token = results.removeFirst()
        assertEquals(RichTextType.UNICODE_EMOTE, token.type)
        assertEquals("\uD83D\uDE02", token.substring)

        token = results.removeFirst()
        assertEquals(RichTextType.TEXT, token.type)
        assertEquals(" <", token.substring)

        token = results.removeFirst()
        assertEquals(RichTextType.UNICODE_EMOTE, token.type)
        assertEquals("\uD83D\uDE02", token.substring)

        token = results.removeFirst()
        assertEquals(RichTextType.TEXT, token.type)
        assertEquals("abc>", token.substring)
    }

    @Test
    fun `Extract emoji aliases ignoring custom emojis named the same as an alias`() {
        // ":joy: <:joy:1235>" => [joy, <text>]

        val results = RichTextFinder(
            /* input = */ ":joy: <:joy:1234>",
            /* getIMentionable = */ false,
            /* getGlobalMentions = */ false,
            /* getEmojis = */ true,
            /* getUrls = */ false,
        ).results

        assertEquals(2, results.size)

        assertEquals(RichTextType.UNICODE_EMOTE, results[0].type)
        assertEquals("\uD83D\uDE02", results[0].substring)

        // This is a TEXT because custom emoji (mentionable) parsing is disabled
        // This test also ensures that the parser works even when custom emoji parsing is disabled
        assertEquals(RichTextType.TEXT, results[1].type)
        assertEquals(" <:joy:1234>", results[1].substring)
    }

    @Test
    fun `Extract emoji aliases ignoring animated custom emojis named the same as an alias`() {
        // ":joy: <a:joy:1235>" => [joy, <text>]

        val results = RichTextFinder(
            /* input = */ ":joy: <a:joy:1234>",
            /* getIMentionable = */ false,
            /* getGlobalMentions = */ false,
            /* getEmojis = */ true,
            /* getUrls = */ false,
        ).results

        assertEquals(2, results.size)

        assertEquals(RichTextType.UNICODE_EMOTE, results[0].type)
        assertEquals("\uD83D\uDE02", results[0].substring)

        // This is a TEXT because custom emoji (mentionable) parsing is disabled
        // This test also ensures that the parser works even when custom emoji parsing is disabled
        assertEquals(RichTextType.TEXT, results[1].type)
        assertEquals(" <a:joy:1234>", results[1].substring)
    }
}
