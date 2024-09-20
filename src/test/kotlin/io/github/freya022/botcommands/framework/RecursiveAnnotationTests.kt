package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.api.core.utils.findAllAnnotations
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.flatMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.lang.annotation.Inherited

private object FindAllAnnotations {
    annotation class MyAnnotation(val name: String)

    @D
    @E
    @C // Test already visited
    @MyAnnotation("B")
    annotation class B
    @F
    @G
    @B // Test already visited
    @MyAnnotation("C")
    annotation class C

    @MyAnnotation("D")
    annotation class D
    @MyAnnotation("E")
    annotation class E
    @MyAnnotation("F")
    annotation class F
    @MyAnnotation("G")
    annotation class G

    @B
    @C
    @MyAnnotation("A")
    annotation class A

    @A
    class MyClass
}

private object OverrideIndirectAnnotations {
    @Repeatable
    annotation class RepeatableAnnotationWithValues(vararg val values: Int)
    annotation class AnnotationWithValues(vararg val values: Int)

    @AnnotationWithValues(3)
    annotation class MetaAnnotationWithValues

    @RepeatableAnnotationWithValues(1)
    @RepeatableAnnotationWithValues(2)
    @MetaAnnotationWithValues
    @AnnotationWithValues(4)
    class MultipleValuesAnnotated
}

private object InheritedAnnotations {
    @Inherited
    annotation class InheritedAnnotation

    @InheritedAnnotation
    abstract class MyAbstractClass

    class MyClass : MyAbstractClass()
}

object RecursiveAnnotationTests {
    @Test
    fun `Find all annotations in order`() {
        val allAnnotations = FindAllAnnotations.MyClass::class.findAllAnnotations<FindAllAnnotations.MyAnnotation>()

        assertEquals(
            listOf(
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
            ),
            allAnnotations.map { it.name }
        )
    }

    @Test
    fun `Override indirect annotations with direct annotation`() {
        val values = OverrideIndirectAnnotations.MultipleValuesAnnotated::class
            .findAllAnnotations<OverrideIndirectAnnotations.AnnotationWithValues>(rootOverride = true)
            .flatMap { it.values.toTypedArray() }

        assertEquals(listOf(4), values)
    }

    @Test
    fun `Override indirect annotations with direct repeatable annotation`() {
        val values = OverrideIndirectAnnotations.MultipleValuesAnnotated::class
            .findAllAnnotations<OverrideIndirectAnnotations.RepeatableAnnotationWithValues>(rootOverride = true)
            .flatMap { it.values.toTypedArray() }

        assertEquals(listOf(1, 2), values)
    }

    @Test
    fun `Merge annotations`() {
        val values = OverrideIndirectAnnotations.MultipleValuesAnnotated::class
            .findAllAnnotations<OverrideIndirectAnnotations.AnnotationWithValues>(rootOverride = false)
            .flatMap { it.values.toTypedArray() }

        // BFS order!
        assertEquals(listOf(4, 3), values)
    }

    @Disabled("Not implemented yet")
    @Test
    fun `Inherited annotations`() {
        assertNotNull(InheritedAnnotations.MyClass::class.findAnnotationRecursive<InheritedAnnotations.InheritedAnnotation>())
    }
}