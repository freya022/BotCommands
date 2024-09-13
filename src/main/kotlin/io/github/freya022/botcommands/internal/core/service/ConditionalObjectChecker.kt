package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.annotations.HardcodedCondition
import io.github.freya022.botcommands.internal.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.internal.utils.isObject
import kotlin.reflect.KClass

internal object ConditionalObjectChecker : ClassGraphProcessor {
    override fun processClass(classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        if (!isService) return
        if (!kClass.isObject) return

        // Taking all (including inherited) annotations using ClassGraph would have been faster and cleaner,
        // but this allows for a much more precise error message, as to which annotation provoked this error
        kClass.annotations.forEach { rootAnnotation ->
            val set: MutableSet<KClass<out Annotation>> = hashSetOf()
            fun KClass<out Annotation>.checkHasCondition(rootAnnotation: KClass<out Annotation>) {
                check(!hasAnnotationRecursive<HardcodedCondition>()) {
                    "Singleton ${kClass.simpleNestedName} cannot use @${rootAnnotation.simpleNestedName} as the object always gets initialized"
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