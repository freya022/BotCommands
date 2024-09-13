package io.github.freya022.botcommands.othertests

import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.utils.findAnnotationRecursive
import java.lang.annotation.Inherited

@Inherited
@BService
annotation class MyAnnotation

@MyAnnotation
class MyClass

fun main() {
    val scan = ClassGraph()
        .enableClassInfo()
        .enableAnnotationInfo()
        .acceptClasses(MyClass::class.java.name)
        .scan()

    val classInfo = scan.getClassInfo(MyClass::class.java.name)
    println(MyClass::class.findAnnotationRecursive<BService>())
}