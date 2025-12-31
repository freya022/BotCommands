package io.github.freya022.botcommands

import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.reflect.full.declaredMemberProperties
import kotlin.test.Test
import kotlin.test.assertEquals

class ReflectionMetadataAccessorTest {
    @Suppress("unused")
    fun function(param: String) {}

    val property: String
        get() = "foo"

    @Test
    fun `Get parameter callable`() {
        val ref = ReflectionMetadataAccessorTest::function

        val func = assertDoesNotThrow { ref.parameters.first().function }
        assertEquals(ref, func)
    }

    @Test
    fun `Get declaring class of unbound property`() {
        val ref = ReflectionMetadataAccessorTest::class.declaredMemberProperties.first { it.name == "property" }

        val clazz = assertDoesNotThrow { ref.declaringClass }
        assertEquals(ReflectionMetadataAccessorTest::class, clazz)
    }

    @Test
    fun `Get declaring class of bound property`() {
        val ref = ::property

        val clazz = assertDoesNotThrow { ref.declaringClass }
        assertEquals(ReflectionMetadataAccessorTest::class, clazz)
    }
}
