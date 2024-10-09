package io.github.freya022.botcommands.internal.modals

import gnu.trove.map.TObjectLongMap
import gnu.trove.map.hash.TObjectLongHashMap
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
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
import io.github.freya022.botcommands.internal.options.transformParameters
import io.github.freya022.botcommands.internal.parameters.*
import io.github.freya022.botcommands.internal.requireUser
import io.github.freya022.botcommands.internal.throwUser
import io.github.freya022.botcommands.internal.utils.*
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.jvmErasure
import io.github.freya022.botcommands.api.modals.annotations.ModalData as ModalDataAnnotation

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

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("For removal, confusing on whether it searches nested parameters, prefer using collection operations on 'parameters' instead, make an extension or an utility method")
    override fun getParameter(declaredName: String): AggregatedParameter? =
        parameters.find { it.name == declaredName }

    internal suspend fun execute(modalData: ModalData, event: ModalEvent) {
        val handlerData = modalData.handlerData as? PersistentModalHandlerData ?: throwInternal("This method should have not been ran as there is no handler data")

        val inputDataMap = modalData.inputDataMap
        val inputNameToInputIdMap: TObjectLongMap<String> = TObjectLongHashMap()
        inputDataMap.forEachEntry { inputId: Long, inputData: InputData ->
            inputNameToInputIdMap.put(inputData.inputName, inputId)
            true
        }

        val userDatas = handlerData.userData

        //Check if there's enough arguments to fit user data + modal inputs
        requireAt(expectedModalDatas == userDatas.size && expectedModalInputs == event.values.size, function) {
            """
                Modal handler does not match the received modal data:
                Method signature: $expectedModalDatas userdata parameters and $expectedModalInputs modal input(s)
                Discord data: ${userDatas.size} userdata parameters and ${event.values.size} modal input(s)
            """.trimIndent()
        }

        val userDataIterator = userDatas.iterator()
        val optionValues = parameters.mapOptions { option ->
            if (tryInsertOption(event, option, inputNameToInputIdMap, userDataIterator, this) == InsertOptionResult.ABORT)
                throwInternal(::tryInsertOption, "Insertion function shouldn't have been aborted")
        }

        function.callSuspendBy(parameters.mapFinalParameters(event, optionValues))
    }

    private suspend fun tryInsertOption(
        event: ModalEvent,
        option: OptionImpl,
        inputNameToInputIdMap: TObjectLongMap<String>,
        userDataIterator: Iterator<Any?>,
        optionMap: MutableMap<OptionImpl, Any?>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as ModalHandlerInputOption

                //We have the modal input's ID
                // But we have a Map of input *name* -> InputData (contains input ID)
                val inputId = inputNameToInputIdMap[option.inputName].takeIf { it != inputNameToInputIdMap.noEntryValue }
                    ?: throwUser("Modal input named '${option.inputName}' was not found")
                val modalMapping = event.getValue(ModalMaps.getInputId(inputId))
                    ?: throwUser("Modal input ID '$inputId' was not found on the event")

                option.resolver.resolveSuspend(option, event, modalMapping).also { obj ->
                    // Technically not required, but provides additional info
                    requireUser(obj != null || option.isOptionalOrNullable) {
                        "The parameter '${option.declaredName}' of value '${modalMapping.asString}' could not be resolved into a ${option.type.simpleNestedName}"
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