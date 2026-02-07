package io.github.freya022.botcommands.core.reflect

import io.github.freya022.botcommands.api.core.reflect.KotlinTypeToken
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

object KotlinTypeTokenTest {
    @Test
    fun `Valid KotlinTypeToken`() {
        assertDoesNotThrow { object : KotlinTypeToken<String>() {} }
        assertDoesNotThrow { object : KotlinTypeToken<List<String>>() {} }
        assertDoesNotThrow { object : KotlinTypeToken<List<*>>() {} }
    }
}
