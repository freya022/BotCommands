package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.modals.annotations.ModalHandler
import com.freya02.botcommands.api.modals.annotations.ModalInput
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.core.reflection.*
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.parameters.OptionParameter
import com.freya02.botcommands.internal.utils.*
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure
import com.freya02.botcommands.api.modals.annotations.ModalData as ModalDataAnnotation

class ModalHandlerInfo(
    val context: BContextImpl,
    override val eventFunction: MemberEventFunction<ModalInteractionEvent, *>
) : IExecutableInteractionInfo {
    override val parameters: List<ModalHandlerParameter>

    private val expectedModalDatas: Int
    private val expectedModalInputs: Int

    val handlerName: String

    init {
        val annotation = function.findAnnotation<ModalHandler>()!!
        handlerName = annotation.name

        parameters = function.nonInstanceParameters.drop(1).transformParameters(
            builderBlock = { function, parameter, declaredName ->
                when {
                    parameter.hasAnnotation<ModalInput>() -> ModalHandlerInputOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    parameter.hasAnnotation<ModalDataAnnotation>() -> ModalHandlerDataOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    else -> CustomOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                }
            },
            aggregateBlock = { ModalHandlerParameter(context, it) }
        )

        val options = parameters.flatMap { it.allOptions }
        expectedModalDatas = options.filterIsInstance<ModalHandlerDataOption>().count()
        expectedModalInputs = options.filterIsInstance<ModalHandlerInputOption>().count()
    }

    @Throws(Exception::class)
    suspend fun execute(modalData: ModalData, event: ModalInteractionEvent): Boolean {
        val handlerData = modalData.handlerData as? PersistentModalHandlerData ?: throwInternal("This method should have not been ran as there is no handler data")

        val inputDataMap = modalData.inputDataMap
        val inputNameToInputIdMap: MutableMap<String, String> = HashMap()
        inputDataMap.forEach { (inputId: String, inputData: InputData) ->
            inputNameToInputIdMap[inputData.inputName] = inputId
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
        inputNameToInputIdMap: Map<String, String>,
        userDataIterator: Iterator<Any>,
        optionMap: MutableMap<Option, Any?>
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as ModalHandlerInputOption

                //We have the modal input's ID
                // But we have a Map of input *name* -> InputData (contains input ID)
                val inputId = inputNameToInputIdMap[option.inputName]
                    ?: throwUser("Modal input named '${option.inputName}' was not found")
                val modalMapping = event.getValue(inputId)
                    ?: throwUser("Modal input ID '$inputId' was not found on the event")

                option.resolver.resolveSuspend(context, this, event, modalMapping).also { obj ->
                    requireUser(obj != null || option.isOptionalOrNullable) {
                        "The parameter '${option.declaredName}' of value '${modalMapping.asString}' could not be resolved into a ${option.type.simpleNestedName}"
                    }
                }
            }

            OptionType.GENERATED -> {
                option as ModalHandlerDataOption

                if (!userDataIterator.hasNext())
                    throwInternal("Mismatch in amount of user data provided by the user and the amount requested by the aggregates, this should have been checked")

                userDataIterator.next().also { userData ->
                    requireUser(option.type.jvmErasure.isSuperclassOf(userData::class)) {
                        "The modal user data '%s' is not a valid type (expected a %s, got a %s)".format(
                            option.declaredName,
                            option.type.simpleNestedName,
                            userData.javaClass.simpleName
                        )
                    }
                }
            }

            OptionType.CUSTOM -> {
                option as CustomMethodOption
                option.resolver.resolveSuspend(context, this, event)
            }

            else -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}