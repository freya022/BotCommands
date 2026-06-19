package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.ClassPathProcessor
import io.github.freya022.botcommands.internal.core.service.annotations.HardcodedCondition
import io.github.freya022.botcommands.internal.utils.isObject
import kotlin.reflect.KClass

internal class ConditionalObjectChecker : ClassPathProcessor {

    override fun processClass(data: ClassPathProcessor.ClassData) {
        if (!data.isService) return
        if (!data.isProbablyObject) return

        // Taking all (including inherited) annotations using ClassGraph would have been faster and cleaner,
        // but this allows for a much more precise error message, as to which annotation provoked this error
        val clazz = data.clazz
        clazz.annotations.forEach { rootAnnotation ->
            val set: MutableSet<KClass<out Annotation>> = hashSetOf()
            fun KClass<out Annotation>.checkHasCondition(rootAnnotation: KClass<out Annotation>) {
                check(!hasAnnotationRecursive<HardcodedCondition>()) {
                    "Singleton ${clazz.simpleNestedName} cannot use @${rootAnnotation.simpleNestedName} as the object always gets initialized"
                }

                annotations.forEach {
                    // Prevent infinite loops
                    if (set.add(it.annotationClass))
                        it.annotationClass.checkHasCondition(rootAnnotation)
                }
            }

            rootAnnotation.annotationClass.checkHasCondition(rootAnnotation.annotationClass)
        }
    }
}
