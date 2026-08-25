package dev.freya02.botcommands.typesafe.messages.utils

import dev.freya02.botcommands.typesafe.messages.internal.utils.NullabilityHelper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

object NullabilityHelperTests {

    private val logger = KotlinLogging.logger { }

    @MethodSource("getNullableParameterIndexesSamples")
    @ParameterizedTest
    fun getNullableParameterIndexes(klass: KClass<*>, function: KFunction<*>, expectedIndexes: Set<Int>) {
        mockkObject(NullabilityHelper) {
            every { NullabilityHelper.loadNullableParameterIndexesFromKtReflect(any()) } answers { fail("Fallback should not be reached") }

            assertEquals(expectedIndexes, NullabilityHelper.getNullableParameterIndexes(logger, klass, function))
        }
    }

    @Test
    fun `Finds nested classes`() {
        mockkObject(NullabilityHelper) {
            every { NullabilityHelper.loadNullableParameterIndexesFromKtReflect(any()) } answers { fail("Fallback should not be reached") }

            NullabilityHelper.getNullableParameterIndexes(logger, Nested::class, Nested::foo)
        }
    }

    @JvmStatic
    fun getNullableParameterIndexesSamples(): List<Arguments> {
        return listOf(
            Arguments.argumentSet("Kotlin type marked nullable", NullabilityHelperTests::class, NullabilityHelperTests::typeMarkedNullable, setOf(0)),
            Arguments.argumentSet("Type-use runtime visible", NullabilityHelperTestsSamples::class, NullabilityHelperTestsSamples::typeUseRuntimeVisible, setOf(0)),
            Arguments.argumentSet("Type-use runtime invisible", NullabilityHelperTestsSamples::class, NullabilityHelperTestsSamples::typeUseRuntimeInvisible, setOf(1)),
            Arguments.argumentSet("Parameter runtime invisible", NullabilityHelperTestsSamples::class, NullabilityHelperTestsSamples::parameterRuntimeInvisible, setOf(2)),
            Arguments.argumentSet("Parameter runtime visible", NullabilityHelperTestsSamples::class, NullabilityHelperTestsSamples::parameterRuntimeVisible, setOf(0)),
        )
    }

    @MethodSource("loadNullableParameterIndexesFromKtReflectSamples")
    @ParameterizedTest
    fun loadNullableParameterIndexesFromKtReflect(function: KFunction<*>, expectedIndexes: Set<Int>) {
        assertEquals(expectedIndexes, NullabilityHelper.loadNullableParameterIndexesFromKtReflect(function))
    }

    @JvmStatic
    fun loadNullableParameterIndexesFromKtReflectSamples(): List<Arguments> {
        return listOf(
            Arguments.argumentSet("Kotlin type marked nullable", NullabilityHelperTests::typeMarkedNullable, setOf(0)),
            // TODO kotlin-reflect doesn't support reading type annotations from Java sources, revisit when the K2 impl is done
//            Arguments.argumentSet("Type-use runtime visible", NullabilityHelperTestsSamples::typeUseRuntimeVisible, setOf(0)),
            Arguments.argumentSet("Parameter runtime visible", NullabilityHelperTestsSamples::parameterRuntimeVisible, setOf(0)),
        )
    }

    internal fun typeMarkedNullable(@Suppress("unused") bar: String?) {}

    private interface Nested {
        fun foo()
    }
}
