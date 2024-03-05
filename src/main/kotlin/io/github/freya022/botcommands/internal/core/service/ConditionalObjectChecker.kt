package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.annotations.HardcodedCondition
import io.github.freya022.botcommands.internal.utils.isObject
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

object ConditionalObjectChecker : ClassGraphProcessor {
    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        if (!isService) return
        if (!kClass.isObject) return

        kClass.annotations.forEach { rootAnnotation ->
            val set: MutableSet<KClass<out Annotation>> = hashSetOf()
            fun KClass<out Annotation>.checkHasCondition(rootAnnotation: KClass<out Annotation>) {
                check(!hasAnnotation<HardcodedCondition>()) {
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