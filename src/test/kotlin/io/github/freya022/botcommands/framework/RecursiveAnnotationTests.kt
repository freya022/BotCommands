package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.api.core.utils.findAllAnnotations
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
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

    @Disabled("Not implemented yet")
    @Test
    fun `Inherited annotations`() {
        assertNotNull(InheritedAnnotations.MyClass::class.findAnnotationRecursive<InheritedAnnotations.InheritedAnnotation>())
    }
}