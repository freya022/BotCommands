package io.github.freya022.botcommands.othertests

import io.github.classgraph.ClassGraph
import kotlin.metadata.Visibility
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.visibility

fun main() {
    val metadataName = Metadata::class.java.name
    val publicInInternalPackage = ClassGraph()
        .acceptPackages("io.github.freya022.botcommands.internal")
        .enableAnnotationInfo()
        .scan()
        .allClasses
        .filter { it.annotationInfo.directOnly().containsName(metadataName) }
        .filter {
            val metadata = KotlinClassMetadata.readStrict(it.annotationInfo.directOnly()[metadataName].loadClassAndInstantiate() as Metadata)
            when (metadata) {
                is KotlinClassMetadata.Class -> {
                    metadata.kmClass.visibility != Visibility.INTERNAL
                }

                is KotlinClassMetadata.FileFacade -> {
                    false
                }

                is KotlinClassMetadata.SyntheticClass -> {
                    false
                }

                else -> {
                    println("Unknown type $metadata")
                    true
                }
            }
        }

    println()
}