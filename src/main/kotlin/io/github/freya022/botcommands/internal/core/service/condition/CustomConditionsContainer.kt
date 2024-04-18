package io.github.freya022.botcommands.internal.core.service.condition

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveReference
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.createSingleton
import io.github.freya022.botcommands.internal.utils.requireUser
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.logger { }

internal class CustomConditionsContainer : ClassGraphProcessor {
    private val _customConditionCheckers: MutableList<CustomConditionInfo> = arrayListOf()
    internal val customConditionCheckers: List<CustomConditionInfo> get() = _customConditionCheckers

    override fun processClass(classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
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
                    append("\tHint: ${customConditionType.simpleNestedName} should implement ${classRef<CustomConditionChecker<*>>()}<$conditionName>")
                }
            }

            @Suppress("UNCHECKED_CAST")
            _customConditionCheckers += CustomConditionInfo(checker as CustomConditionChecker<Annotation>, conditionMetadata)
        }
    }

    override fun postProcess() {
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