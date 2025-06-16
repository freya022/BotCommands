package io.github.freya022.botcommands.properties.processor.utils

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument

val KSDeclaration.canonicalName: String
    get() {
        tailrec fun String.prependDeclaringClass(declaration: KSDeclaration): String {
            val parent = declaration.parentDeclaration ?: return this
            return (parent.simpleName.asString() + "$" + this).prependDeclaringClass(parent)
        }

        val parent = parentDeclaration ?: return ""
        return parent.packageName.asString() + "." + parent.simpleName.asString().prependDeclaringClass(parent)
    }

fun <R : Any> KSAnnotation.getIfSet(name: String): R? {
    return arguments.get<R>(name).takeIf { defaultArguments.get<R>(name) != it }
}

fun <R : Any> KSAnnotation.getOrDefault(name: String): R {
    return arguments[name]
}

context(KSAnnotation)
@Suppress("UNCHECKED_CAST")
private operator fun <R : Any> Iterable<KSValueArgument>.get(name: String): R =
    singleOrNull { it.name?.asString() == name }?.value as R?
        ?: throw IllegalArgumentException("Could not find an argument named '$name' on ${shortName.asString()}")

fun KSAnnotated.findAnnotation(annotationName: AnnotationName): KSAnnotation {
    return annotations.single { it.isA(annotationName) }
}

fun KSAnnotated.findAnnotationOrNull(annotationName: AnnotationName): KSAnnotation? {
    return annotations.singleOrNull { it.isA(annotationName) }
}

fun KSAnnotated.isAnnotationPresent(annotationName: AnnotationName): Boolean {
    return annotations.any { it.isA(annotationName) }
}