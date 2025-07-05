package io.github.freya022.botcommands.othertests

import io.github.freya022.botcommands.api.core.reflect.KotlinTypeToken
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

object KotlinTypeTokenTest {
    @Test
    fun `Valid KotlinTypeToken`() {
        assertDoesNotThrow { object : KotlinTypeToken<String>() {} }
        assertDoesNotThrow { object : KotlinTypeToken<List<String>>() {} }
        assertDoesNotThrow { object : KotlinTypeToken<List<*>>() {} }
    }

    @Test
    fun `Invalid KotlinTypeToken`() {
        assertThrows<IllegalArgumentException> { genericFunction<String>() }
        assertThrows<IllegalArgumentException> { reifiedGenericFunction<String>() }
    }

    private fun <T> genericFunction() {
        object : KotlinTypeToken<T>() {}
    }

    // KType#classifier returns null, but KType#javaType works,
    // while it should be supported, the APIs use KType, and you can't get one from a Type.
    private inline fun <reified T> reifiedGenericFunction(): KotlinTypeToken<T> {
        return object : KotlinTypeToken<T>() {}
    }
}