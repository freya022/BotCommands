package dev.freya02.botcommands.spring.metadata.generator.utils

import io.github.classgraph.AnnotationInfo

@Suppress("UNCHECKED_CAST")
internal fun <R : Any> AnnotationInfo.getIfSet(name: String): R? {
    return getParameterValues(false)[name]?.value as R?
}

@Suppress("UNCHECKED_CAST")
internal fun <R : Any> AnnotationInfo.getOrDefault(name: String): R {
    return getParameterValues(true)[name].value as R
}

@Suppress("UNCHECKED_CAST")
internal fun <R : Any> AnnotationInfo.get(name: String): R {
    return getParameterValues(false)[name].value as R
}
