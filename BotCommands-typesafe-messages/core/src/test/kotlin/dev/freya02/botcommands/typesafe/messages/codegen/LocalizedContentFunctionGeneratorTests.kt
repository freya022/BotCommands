package dev.freya02.botcommands.typesafe.messages.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnsupportedParameterException
import dev.freya02.botcommands.typesafe.messages.internal.codegen.LocalizedContentFunctionGenerator
import dev.freya02.botcommands.typesafe.messages.internal.utils.NullabilityHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.assertThrows
import java.lang.classfile.ClassBuilder
import java.lang.constant.ClassDesc
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertTrue

class LocalizedContentFunctionGeneratorTests {

    @Test
    fun `Rejects boxed params`() {
        val exception = assertThrows<UnsupportedParameterException> {
            mockkObject(NullabilityHelper) {
                // Reduce required mocks, we don't need nullabilities
                every { NullabilityHelper.getNullableParameterIndexes(any(), any(), any()) } returns emptySet()

                // TODO Replace by real function after https://youtrack.jetbrains.com/issue/KT-88884 is fixed
                val function = mockk<KFunction<*>> {
                    every { isSuspend } returns false
                    every { returnType } returns typeOf<String>()
                    every { annotations } returns listOf(LocalizedContent("key"))
                    every { parameters } returns listOf(
                        mockk<KParameter> {
                            every { kind } returns KParameter.Kind.VALUE
                            every { type } returns typeOf<Character>()
                            every { isOptional } returns false
                            every { index } returns 0
                        }
                    )
                }

                LocalizedContentFunctionGenerator.create(mockk<ClassDesc>(), mockk<ClassBuilder>(), mockk<KClass<out IMessageSource>>(), function)
            }
        }

        assertTrue(exception.message!!.startsWith("Boxed primitive parameters are not allowed!"), message = "Actual message: ${exception.message}")
    }
}
