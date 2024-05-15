package io.github.freya022.botcommands.internal.modals

import gnu.trove.map.TObjectLongMap
import gnu.trove.map.hash.TObjectLongHashMap
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.parameters.ServiceMethodOption
import io.github.freya022.botcommands.internal.parameters.toServiceOrCustomOptionBuilder
import io.github.freya022.botcommands.internal.requireUser
import io.github.freya022.botcommands.internal.throwUser
import io.github.freya022.botcommands.internal.transformParameters
import io.github.freya022.botcommands.internal.utils.*
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure
import io.github.freya022.botcommands.api.modals.annotations.ModalData as ModalDataAnnotation

class ModalHandlerInfo internal constructor(
    context: BContextImpl,
    override val eventFunction: MemberParamFunction<ModalInteractionEvent, *>
) : IExecutableInteractionInfo {
    override val parameters: List<ModalHandlerParameter>

    private val expectedModalDatas: Int
    private val expectedModalInputs: Int

    val handlerName: String

    init {
        val annotation = function.findAnnotation<ModalHandler>()!!
        handlerName = annotation.name

        parameters = eventFunction.transformParameters(
            builderBlock = { function, parameter, declaredName ->
                val optionParameter = OptionParameter.fromSelfAggregate(function, declaredName)
                if (parameter.hasAnnotation<ModalInput>()) {
                    ModalHandlerInputOptionBuilder(optionParameter)
                } else if (parameter.hasAnnotation<ModalDataAnnotation>()) {
                    ModalHandlerDataOptionBuilder(optionParameter)
                } else {
                    optionParameter.toServiceOrCustomOptionBuilder(context.serviceContainer)
                }
            },
            aggregateBlock = { ModalHandlerParameter(context, it) }
        )

        val options = parameters.flatMap { it.allOptions }
        expectedModalDatas = options.filterIsInstance<ModalHandlerDataOption>().count()
        expectedModalInputs = options.filterIsInstance<ModalHandlerInputOption>().count()
    }

    internal suspend fun execute(modalData: ModalData, event: ModalInteractionEvent): Boolean {
        val handlerData = modalData.handlerData as? PersistentModalHandlerData ?: throwInternal("This method should have not been ran as there is no handler data")

        val inputDataMap = modalData.inputDataMap
        val inputNameToInputIdMap: TObjectLongMap<String> = TObjectLongHashMap()
        inputDataMap.forEachEntry { inputId: Long, inputData: InputData ->
            inputNameToInputIdMap.put(inputData.inputName, inputId)
            true
        }

        val userDatas = handlerData.userData

        //Check if there's enough arguments to fit user data + modal inputs
        requireUser(expectedModalDatas == userDatas.size && expectedModalInputs == event.values.size, function) {
            """
            Modal handler does not match the received modal data:
            Method signature: $expectedModalDatas userdata parameters and $expectedModalInputs modal input(s)
            Discord data: ${userDatas.size} userdata parameters and ${event.values.size} modal input(s)""".trimIndent()
        }

        val userDataIterator = userDatas.iterator()
        val optionValues = parameters.mapOptions { option ->
            if (tryInsertOption(event, option, inputNameToInputIdMap, userDataIterator, this) == InsertOptionResult.ABORT)
                throwInternal(::tryInsertOption, "Insertion function shouldn't have been aborted")
        }

        function.callSuspendBy(parameters.mapFinalParameters(event, optionValues))

        return true
    }

    private suspend fun tryInsertOption(
        event: ModalInteractionEvent,
        option: Option,
        inputNameToInputIdMap: TObjectLongMap<String>,
        userDataIterator: Iterator<Any?>,
        optionMap: MutableMap<Option, Any?>
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

                option.resolver.resolveSuspend(this, event, modalMapping).also { obj ->
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
                option.resolver.resolveSuspend(this, event)
            }

            OptionType.SERVICE -> (option as ServiceMethodOption).getService()

            OptionType.CONSTANT -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}