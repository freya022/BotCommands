package dev.freya02.botcommands.spring.metadata.generator

internal class AnnotationName(
    val packageName: String,
    val simpleName: String
) {
    val name: String
        get() = "$packageName.$simpleName"
}
