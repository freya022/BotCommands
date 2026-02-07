package dev.freya02.botcommands.jda.keepalive.internal.transformer

import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassElement
import java.lang.classfile.ClassTransform

internal interface ContextualClassTransform : ClassTransform {

    override fun atStart(builder: ClassBuilder): Unit = context(builder) { atStartContextual() }

    context(classBuilder: ClassBuilder)
    fun atStartContextual() { }


    override fun atEnd(builder: ClassBuilder): Unit = context(builder) { atEndContextual() }

    context(classBuilder: ClassBuilder)
    fun atEndContextual() { }


    override fun accept(builder: ClassBuilder, element: ClassElement): Unit = context(builder) { acceptContextual(element) }

    context(classBuilder: ClassBuilder)
    fun acceptContextual(classElement: ClassElement) { }
}
