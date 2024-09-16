package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.internal.utils.findAllAnnotations
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
}