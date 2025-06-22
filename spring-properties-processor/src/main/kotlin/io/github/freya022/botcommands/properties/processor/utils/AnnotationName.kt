package io.github.freya022.botcommands.properties.processor.utils

import com.google.devtools.ksp.symbol.KSAnnotation

class AnnotationName(
    val packageName: String,
    val simpleName: String
) {
    val name: String
        get() = "$packageName.$simpleName"
}

fun KSAnnotation.isA(annotationName: AnnotationName): Boolean =
    shortName.asString() == annotationName.simpleName &&
            annotationType.resolve().declaration.qualifiedName!!.asString() == annotationName.name