package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.annotations.ModalData as ModalDataAnnotation
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import io.github.freya022.botcommands.internal.modals.options.ModalHandlerDataOption
import io.github.freya022.botcommands.internal.modals.options.ModalHandlerInputOption
import io.github.freya022.botcommands.internal.modals.options.ModalHandlerParameterImpl
import io.github.freya022.botcommands.internal.modals.options.builder.ModalHandlerDataOptionBuilderImpl
import io.github.freya022.botcommands.internal.modals.options.builder.ModalHandlerInputOptionBuilderImpl
import io.github.freya022.botcommands.internal.modals.utils.valueAsString
import io.github.freya022.botcommands.internal.options.transformParameters
import io.github.freya022.botcommands.internal.parameters.*
import io.github.freya022.botcommands.internal.requireUser
import io.github.freya022.botcommands.internal.throwUser
import io.github.freya022.botcommands.internal.utils.*
import kotlin.reflect.jvm.jvmErasure

internal class ModalHandlerInfo internal constructor(
    override val context: BContextImpl,
    override val eventFunction: MemberParamFunction<ModalEvent, *>
) : ExecutableMixin {
    override val parameters: List<ModalHandlerParameterImpl>

    private val expectedModalDatas: Int
    private val expectedModalInputs: Int

    internal val handlerName: String

    init {
        val annotation = function.findAnnotationRecursive<ModalHandler>()!!
        handlerName = annotation.name

        val resolverContainer = context.serviceContainer.getService<ResolverContainer>()
        parameters = eventFunction.transformParameters(
            builderBlock = { function, parameter, declaredName ->
                val optionParameter = OptionParameter.fromSelfAggregate(function, declaredName)
                if (parameter.hasAnnotationRecursive<ModalInput>()) {
                    ModalHandlerInputOptionBuilderImpl(optionParameter)
                } else if (parameter.hasAnnotationRecursive<ModalDataAnnotation>()) {
                    ModalHandlerDataOptionBuilderImpl(optionParameter)
                } else {
                    optionParameter.toFallbackOptionBuilder(context.serviceContainer, resolverContainer)
                }
            },
            aggregateBlock = { ModalHandlerParameterImpl(this, it) }
        )

        val options = parameters.flatMap { it.allOptions }
        expectedModalDatas = options.filterIsInstance<ModalHandlerDataOption>().count()
        expectedModalInputs = options.filterIsInstance<ModalHandlerInputOption>().count()
    }

    internal suspend fun execute(modalData: ModalData, event: ModalEvent) {
        val handlerData = modalData.handlerData as? PersistentModalHandlerData ?: throwInternal("This method should have not been ran as there is no handler data")
        val userDatas = handlerData.userData

        //Check if there's enough arguments to fit user data
        requireAt(expectedModalDatas == userDatas.size, function) {
            """
                Modal handler does not match the received modal data:
                Method signature: $expectedModalDatas userdata parameters
                Discord data: ${userDatas.size} userdata parameters
            """.trimIndent()
        }

        val userDataIterator = userDatas.iterator()
        val optionValues = parameters.mapOptions { option ->
            if (tryInsertOption(event, option, userDataIterator, this) == InsertOptionResult.ABORT)
                throwInternal(::tryInsertOption, "Insertion function shouldn't have been aborted")
        }

        methodAccessor.callSuspend(parameters.mapFinalParameters(event, optionValues))
    }

    private suspend fun tryInsertOption(
        event: ModalEvent,
        option: OptionImpl,
        userDataIterator: Iterator<Any?>,
        optionMap: MutableMap<OptionImpl, Any?>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as ModalHandlerInputOption

                //We have the modal input's ID
                // But we have a Map of input *name* -> InputData (contains input ID)
                val modalMapping = event.getValue(option.customId)
                    ?: throwUser("Modal input with custom ID '${option.customId}' was not found on the event, available values: ${event.values.map { it.customId }}")

                option.resolver.resolveSuspend(option, event, modalMapping).also { obj ->
                    // Technically not required, but provides additional info
                    requireUser(obj != null || option.isOptionalOrNullable) {
                        "The parameter '${option.declaredName}' from $modalMapping and value '${modalMapping.valueAsString}' is required but could not be resolved into a ${option.type.simpleNestedName}"
                    }
                }
            }

            OptionType.GENERATED -> {
                option as ModalHandlerDataOption

                if (!userDataIterator.hasNext())
                    throwInternal("Mismatch in amount of user data provided by the user and the amount requested by the aggregates, this should have been checked")

                val userData = userDataIterator.next()
                if (userData != null) {
                    requireUser(option.type.jvmErasure.isInstance(userData)) {
                        "The modal user data '${option.declaredName}' is not a valid type (expected a ${option.type.simpleNestedName}, got a ${userData.javaClass.simpleName})"
                    }
                }

                userData
            }

            OptionType.CUSTOM -> {
                option as CustomMethodOption
                option.resolver.resolveSuspend(option, event)
            }

            OptionType.SERVICE -> (option as ServiceMethodOption).getService()

            OptionType.CONSTANT -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}
