package io.github.freya022.botcommands.othertests

import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.internal.core.service.ConditionalObjectChecker
import io.github.freya022.botcommands.internal.utils.isObject
import kotlin.metadata.ClassKind
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.kind
import kotlin.time.Duration
import kotlin.time.measureTime

// In no way scientific at all
fun main() {
    var d1 = Duration.ZERO
    var d2 = Duration.ZERO
    var d3 = Duration.ZERO
    val kClass = ConditionalObjectChecker::class
    ClassGraph()
        .acceptPackages("io.github.freya022.botcommands")
        .enableAnnotationInfo()
        .scan()
        .allClasses
        .forEach {
            val metadataAnnotation = it.annotationInfo
                .directOnly()
                .get(Metadata::class.java.name)
                ?.loadClassAndInstantiate() as Metadata? ?: return@forEach
            d3 += measureTime {
                kClass.objectInstance
            }
            d1 += measureTime {
                kClass.isObject
            }
            d2 += measureTime {
                metadataAnnotation.isObject()
            }
        }
    println()
}

fun Metadata.isObject(): Boolean {
    val metadata = KotlinClassMetadata.readStrict(this)
    val clazz = metadata as? KotlinClassMetadata.Class ?: return false
    return clazz.kmClass.kind == ClassKind.OBJECT
}