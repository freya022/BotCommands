package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.modals.annotations.ModalHandler
import com.freya02.botcommands.api.modals.annotations.ModalInput
import com.freya02.botcommands.api.parameters.ModalParameterResolver
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
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
) : ExecutableInteractionInfo {

    override val parameters: MethodParameters

    val handlerName: String

    init {
        val annotation = method.findAnnotation<ModalHandler>()!!
        handlerName = annotation.name

        @Suppress("RemoveExplicitTypeArguments") //Kotlin: Could not load module <Error module> --> Type inference is broken
        parameters = MethodParameters.transform<ModalParameterResolver<*, *>>(context, method) {
            optionPredicate = { it.hasAnnotation<ModalInput>() }
            optionTransformer = { parameter, _, resolver -> ModalHandlerInputParameter(parameter, resolver) }

            resolvablePredicate = { it.hasAnnotation<ModalDataAnnotation>() }
            resolvableTransformer = { parameter -> ModalHandlerDataParameter(parameter) }
        }

        val hasModalData = parameters.filterIsInstance<ModalHandlerDataParameter>().isNotEmpty()

        //Check if the first parameters are all modal data
        if (hasModalData) {
            var sawModalData = false
            for (parameter in parameters) {
                if (parameter.methodParameterType != MethodParameterType.OPTION) continue

                parameter as ModalHandlerParameter

                requireUser(parameter is ModalHandlerDataParameter || sawModalData) {
                    """
                    Parameter #${parameter.index} must be annotated with @${ModalData::class.java.simpleName} or situated after all modal data parameters.
                    All modal data must be inserted after the event, with the same order as the constructed modal, before inserting modal inputs and custom parameters""".trimIndent()
                }

                if (parameter is ModalHandlerDataParameter) sawModalData = true
            }
        }
    }

    @Throws(Exception::class)
    suspend fun execute(
        context: BContext,
        modalData: ModalData,
        event: ModalInteractionEvent
    ): Boolean {
        val inputDataMap = modalData.inputDataMap
        val inputNameToInputIdMap: MutableMap<String, String> = HashMap()
        inputDataMap.forEach { (inputId: String, inputData: InputData) ->
            inputNameToInputIdMap[inputData.inputName] = inputId
        }

        val objects: MutableMap<KParameter, Any?> = hashMapOf()
        objects[method.instanceParameter!!] = instance
        objects[method.valueParameters.first()] = event

        val userDatas = modalData.userData
        val expectedModalDatas = parameters.filterIsInstance<ModalHandlerDataParameter>().count()
        val expectedModalInputs = parameters.filterIsInstance<ModalHandlerInputParameter>().count()

        //Check if there's enough arguments to fit user data + modal inputs
        requireUser(expectedModalDatas == userDatas.size && expectedModalInputs == event.values.size) {
            """
            Modal handler does not match the received modal data:
            Method signature: $expectedModalDatas userdata parameters and $expectedModalInputs modal input(s)
            Discord data: ${userDatas.size} userdata parameters and ${event.values.size} modal input(s)""".trimIndent()
        }

        //Insert modal data in the order of appearance, after the event
        for (i in userDatas.indices) {
            val parameter = parameters[i]
            if (parameter.methodParameterType != MethodParameterType.OPTION) continue

            requireUser(parameter is ModalHandlerDataParameter) {
                """
                Parameter #$i must be annotated with @${ModalData::class.java.simpleName} or situated after all modal data parameters.
                All modal data must be inserted after the event, with the same order as the constructed modal, before inserting modal inputs and custom parameters""".trimIndent()
            }

            val userData = userDatas[i]
            requireUser(parameter.type.jvmErasure.isSuperclassOf(userData::class)) {
                "The modal user data '%s' is not a valid type (expected a %s, got a %s)".format(
                    parameter.name,
                    parameter.type.simpleName,
                    userData.javaClass.simpleName
                )
            }

            objects[parameter.kParameter] = userData
        }

        for (parameter in parameters) {
            objects[parameter.kParameter] = when (parameter.methodParameterType) {
                MethodParameterType.OPTION -> {
                    if (parameter !is ModalHandlerInputParameter) continue

                    //We have the modal input's ID
                    // But we have a Map of input *name* -> InputData (contains input ID)
                    val inputId = inputNameToInputIdMap[parameter.inputName]
                        ?: throwUser("Modal input named '${parameter.inputName}' was not found")
                    val modalMapping = event.getValue(inputId)
                        ?: throwUser("Modal input ID '$inputId' was not found on the event")

                    parameter.resolver.resolveSuspend(context, this, event, modalMapping).also { obj ->
                        requireUser(obj != null || parameter.isOptional) {
                            "The parameter '${parameter.name}' of value '${modalMapping.asString}' could not be resolved into a ${parameter.type.simpleName}"
                        }
                    }
                }
                MethodParameterType.CUSTOM -> {
                    parameter as CustomMethodParameter
                    parameter.resolver.resolveSuspend(context, this, event)
                }
                else -> throwInternal("Unexpected MethodParameterType: ${parameter.methodParameterType}")
            }
        }

        method.callSuspendBy(objects)

        return true
    }
}