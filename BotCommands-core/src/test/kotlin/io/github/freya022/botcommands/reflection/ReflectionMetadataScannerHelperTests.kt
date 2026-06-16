package io.github.freya022.botcommands.reflection

import dev.freya02.bc.reflection.metadata.ReflectionMetadataScannerHelper
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

object ReflectionMetadataScannerHelperTests {
    @ParameterizedTest
    @MethodSource("classNamesToCheck")
    fun `Check class names exist`(className: String) {
        assertDoesNotThrow { Class.forName(className, false, Thread.currentThread().contextClassLoader) }
    }

    @JvmStatic
    fun classNamesToCheck(): List<String> {
        return ReflectionMetadataScannerHelper.classNamesToCheck
    }
}
