package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.ClassGraphProcessor
import com.freya02.botcommands.api.core.service.CustomConditionChecker
import com.freya02.botcommands.api.core.service.annotations.Condition
import com.freya02.botcommands.api.core.utils.joinAsList
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.internal.utils.ReflectionUtils.resolveReference
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import com.freya02.botcommands.internal.utils.createSingleton
import com.freya02.botcommands.internal.utils.requireUser
import io.github.classgraph.ClassInfo
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.logger { }

internal class CustomConditionsContainer : ClassGraphProcessor {
    private val _customConditionCheckers: MutableList<CustomConditionInfo> = arrayListOf()
    internal val customConditionCheckers: List<CustomConditionInfo> get() = _customConditionCheckers

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>) {
        // kClass is the condition, i.e., the meta-annotated class

        if (classInfo.annotationInfo.directOnly().containsName(Condition::class.jvmName)) {
            val conditionMetadata = kClass.findAnnotation<Condition>()!!
            val customConditionType = conditionMetadata.type
            val checker = customConditionType.createSingleton()
            val checkerAnnotationType = checker.annotationType
            requireUser(checkerAnnotationType == kClass.java) {
                buildString {
                    val conditionName = classInfo.simpleNestedName
                    val annotationTypeGetter = checker::annotationType.resolveReference(checker::class)!!.getter

                    append(annotationTypeGetter.shortSignature)
                    append(" : ")
                    appendLine("Condition @$conditionName uses ${customConditionType.simpleNestedName} but its implementation requires @${checkerAnnotationType.simpleNestedName}")
                    append("\tHint: ${customConditionType.simpleNestedName} should implement ${CustomConditionChecker::class.simpleNestedName}<$conditionName>")
                }
            }

            @Suppress("UNCHECKED_CAST")
            _customConditionCheckers += CustomConditionInfo(checker as CustomConditionChecker<Annotation>, conditionMetadata)
        }
    }

    override fun postProcess(context: BContext) {
        if (customConditionCheckers.isNotEmpty()) {
            logger.trace {
                val checkersList = customConditionCheckers.joinAsList {
                    "${it.checker.javaClass.simpleNestedName} (${it.checker.annotationType.simpleNestedName})"
                }
                "Loaded ${customConditionCheckers.size} custom conditions:\n$checkersList"
            }
        } else {
            logger.debug { "Loaded ${customConditionCheckers.size} custom conditions" }
        }
    }
}