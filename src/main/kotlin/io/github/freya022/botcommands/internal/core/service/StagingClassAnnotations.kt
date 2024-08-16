package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.config.BServiceConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * Only used temporarily before [ClassAnnotationsMap] is created using this data
 */
internal class StagingClassAnnotations internal constructor(private val serviceConfig: BServiceConfig) {
    internal val processor = Processor()

    internal inner class Processor internal constructor() : ClassGraphProcessor {
        override fun processClass(
            classInfo: ClassInfo,
            kClass: KClass<*>,
            isDefaultService: Boolean,
            isSpringService: Boolean
        ) {
            if (!isDefaultService && !isSpringService) return

            //Fill map with all the @Command, @Resolver, etc... declarations
            classInfo.annotationInfo.directOnly().forEach { annotationInfo ->
                @Suppress("DEPRECATION")
                if (serviceConfig.serviceAnnotations.any { it.jvmName == annotationInfo.name }) {
                    put(
                        annotationReceiver = kClass,
                        annotationType = annotationInfo.classInfo.loadClass(Annotation::class.java).kotlin
                    )
                }
            }
        }
    }

    // Annotation type => Classes with said annotation
    private val _annotatedClasses: MutableMap<KClass<out Annotation>, MutableSet<KClass<*>>> = hashMapOf()

    internal val annotatedClasses: Map<KClass<out Annotation>, Set<KClass<*>>>
        get() = _annotatedClasses

    private fun put(annotationReceiver: KClass<*>, annotationType: KClass<Annotation>) {
        val annotatedClasses = _annotatedClasses.computeIfAbsent(annotationType) { hashSetOf() }
        // An annotation type cannot be present twice on a function, that wouldn't compile
        if (!annotatedClasses.add(annotationReceiver))
            throwInternal("An annotation instance of type '${annotationType.simpleNestedName}' already exists on class '${annotationReceiver.simpleNestedName}'")
    }
}