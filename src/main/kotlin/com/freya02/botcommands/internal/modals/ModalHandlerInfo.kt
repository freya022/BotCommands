package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.api.modals.annotations.ModalHandler
import com.freya02.botcommands.api.modals.annotations.ModalInput
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.core.options.AbstractOption
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.parameters.OptionParameter
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.set
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure
import com.freya02.botcommands.api.modals.annotations.ModalData as ModalDataAnnotation

class ModalHandlerInfo(
    context: BContextImpl,
    override val instance: Any,
    override val method: KFunction<*>
) : IExecutableInteractionInfo {
    override val parameters: List<ModalHandlerParameter>

    private val options: List<AbstractOption>
    private val expectedModalDatas: Int
    private val expectedModalInputs: Int

    val handlerName: String

    init {
        val annotation = method.findAnnotation<ModalHandler>()!!
        handlerName = annotation.name

        parameters = method.nonInstanceParameters.drop(1).transformParameters(
            builderBlock = { function, parameter, declaredName ->
                when {
                    parameter.hasAnnotation<ModalInput>() -> ModalHandlerInputOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    parameter.hasAnnotation<ModalDataAnnotation>() -> ModalHandlerDataOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    else -> CustomOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                }
            },
            aggregateBlock = { ModalHandlerParameter(context, it) }
        )

        options = parameters.flatMap { it.options }
        expectedModalDatas = options.filterIsInstance<ModalHandlerDataOption>().count()
        expectedModalInputs = options.filterIsInstance<ModalHandlerInputOption>().count()

//        val hasModalData = options.any { it is ModalHandlerDataOption }
//
//        //Check if the first parameters are all modal data
//        if (hasModalData) {
//            var sawModalData = false
//            for (option in options.filterIsInstance<ModalHandlerOption>()) {
//                if (option.methodParameterType != MethodParameterType.OPTION) continue
//
//                requireUser(option is ModalHandlerDataOption || sawModalData, option.kParameter.function) {
//                    """
//                    Parameter #${option.index} must be annotated with @${ModalData::class.java.simpleName} or situated after all modal data parameters.
//                    All modal data must be inserted after the event, with the same order as the constructed modal, before inserting modal inputs and custom parameters""".trimIndent()
//                }
//
//                if (option is ModalHandlerDataOption) sawModalData = true
//            }
//        }
    }

    @Throws(Exception::class)
    suspend fun execute(
        context: BContext,
        modalData: ModalData,
        event: ModalInteractionEvent
    ): Boolean {
        val handlerData = modalData.handlerData as? PersistentModalHandlerData ?: throwInternal("This method should have not been ran as there is no handler data")

        val inputDataMap = modalData.inputDataMap
        val inputNameToInputIdMap: MutableMap<String, String> = HashMap()
        inputDataMap.forEach { (inputId: String, inputData: InputData) ->
            inputNameToInputIdMap[inputData.inputName] = inputId
        }

        val objects: MutableMap<KParameter, Any?> = hashMapOf()
        objects[method.instanceParameter!!] = instance
        objects[method.valueParameters.first()] = event

        val userDatas = handlerData.userData

        //Check if there's enough arguments to fit user data + modal inputs
        requireUser(expectedModalDatas == userDatas.size && expectedModalInputs == event.values.size, method) {
            """
            Modal handler does not match the received modal data:
            Method signature: $expectedModalDatas userdata parameters and $expectedModalInputs modal input(s)
            Discord data: ${userDatas.size} userdata parameters and ${event.values.size} modal input(s)""".trimIndent()
        }

        val userDataIterator = userDatas.iterator()

        //Insert modal data in the order of appearance, after the event
//        for (i in userDatas.indices) {
//            val parameter = parameters[i]
//            if (parameter.methodParameterType != MethodParameterType.OPTION) continue
//
//            requireUser(parameter is ModalHandlerDataParameter) {
//                """
//                Parameter #$i must be annotated with @${ModalData::class.java.simpleName} or situated after all modal data parameters.
//                All modal data must be inserted after the event, with the same order as the constructed modal, before inserting modal inputs and custom parameters""".trimIndent()
//            }
//
//            val userData = userDatas[i]
//            requireUser(parameter.type.jvmErasure.isSuperclassOf(userData::class)) {
//                "The modal user data '%s' is not a valid type (expected a %s, got a %s)".format(
//                    parameter.name,
//                    parameter.type.simpleName,
//                    userData.javaClass.simpleName
//                )
//            }
//
//            objects[parameter.kParameter] = userData
//        }

        for (parameter in parameters) {
            objects[parameter.kParameter] = computeAggregate(context, event, parameter, inputNameToInputIdMap, userDataIterator)
        }

        method.callSuspendBy(objects)

        return true
    }

    private suspend fun computeAggregate(
        context: BContext,
        event: ModalInteractionEvent,
        parameter: ModalHandlerParameter,
        inputNameToInputIdMap: Map<String, String>,
        userDataIterator: Iterator<Any>
    ): Any? {
        val aggregator = parameter.aggregator
        val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
        arguments[aggregator.instanceParameter!!] = parameter.aggregatorInstance
        arguments[aggregator.valueParameters.first()] = event

        for (option in parameter.options) {
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
                        requireUser(obj != null || option.isOptional) {
                            "The parameter '${option.declaredName}' of value '${modalMapping.asString}' could not be resolved into a ${option.type.simpleName}"
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
                                option.type.simpleName,
                                userData.javaClass.simpleName
                            )
                        }
                    }
                }
                OptionType.CUSTOM -> {
                    option as CustomMethodOption
                    option.resolver.resolveSuspend(context, this, event)
                }
                else -> throwInternal("Unexpected MethodParameterType: ${option.optionType}")
            }

            arguments[option] = value
        }

        return aggregator.callSuspendBy(arguments)
    }
}