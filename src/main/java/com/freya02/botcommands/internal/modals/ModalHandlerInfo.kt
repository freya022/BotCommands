package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.runner.MethodRunner
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import java.util.function.Consumer
import kotlin.reflect.KFunction

class ModalHandlerInfo(
    context: BContextImpl,
    override val instance: Any,
    override val method: KFunction<*>
) :
    ExecutableInteractionInfo {
    override val methodRunner: MethodRunner
        get() = TODO()
    val handlerName: String
        get() = TODO()
    override val parameters: MethodParameters
        get() = TODO()

    init {
        TODO()

//        methodRunner = object : MethodRunner {
//            //TODO replace
//            @Suppress("UNCHECKED_CAST")
//            override fun <R> invoke(
//                args: Array<Any>,
//                throwableConsumer: Consumer<Throwable>,
//                successCallback: ConsumerEx<R>
//            ) {
//                try {
//                    val call = method.call(*args)
//                    successCallback.accept(call as R)
//                } catch (e: Throwable) {
//                    throwableConsumer.accept(e)
//                }
//            }
//        }
//
//        val annotation = method.findAnnotation<ModalHandler>()!! //TODO be gone
//        handlerName = annotation.name
//        parameters = MethodParameters.of<ModalParameterResolver>(method, listOf(ModalDataAnnotation::class, ModalInput::class)) { _, _, parameter, resolver ->
//            ModalHandlerParameter(parameter, resolver)
//        }
//        val hasModalData = parameters.filterIsInstance<ModalHandlerParameter>().any { it.isModalData }

//        //Check if the first parameters are all modal data
//        if (hasModalData) {
//            var sawModalData = false
//            for (parameter in parameters) {
//                requireUser(parameter.isModalData || sawModalData) {
//                    """Parameter #${parameter.index} must be annotated with @${ModalData::class.java.simpleName} or situated after all modal data parameters.
//All modal data must be inserted after the event, with the same order as the constructed modal, before inserting modal inputs and custom parameters"""
//                }
//                if (parameter.isModalData) sawModalData = true
//            }
//        }
    }

    @Throws(Exception::class)
    fun execute(
        context: BContext,
        modalData: ModalData,
        event: ModalInteractionEvent,
        throwableConsumer: Consumer<Throwable>
    ): Boolean {
        TODO()
//        val inputDataMap = modalData.inputDataMap
//        val inputNameToInputIdMap: MutableMap<String, String> = HashMap()
//        inputDataMap.forEach { (inputId: String, inputData: InputData) ->
//            inputNameToInputIdMap[inputData.inputName] = inputId
//        }
//        val objects: MutableList<Any?> = ArrayList(parameters.size + 1)
//        objects.add(event)
//        val userData = modalData.userData
//        val expectedModalDatas = parameters.stream().filter { obj: ModalHandlerParameter -> obj.isModalData }.count()
//        val expectedModalInputs = parameters.stream().filter { obj: ModalHandlerParameter -> obj.isModalInput }.count()
//
//        //Check if there's enough arguments to fit user data + modal inputs
//        requireUser(expectedModalDatas == userData.size.toLong() && expectedModalInputs == event.values.size.toLong()) {
//            """Modal handler does not match the received modal data:
//Method signature: $expectedModalDatas userdata parameters and $expectedModalInputs modal input(s)
//Discord data: ${userData.size} userdata parameters and ${event.values.size} modal input(s)"""
//        }
//
//        //Insert modal data in the order of appearance, after the event
//        for (i in userData.indices) {
//            val parameter = parameters[i]
//            requireUser(parameter.isModalData) {
//                """Parameter #$i must be annotated with @${ModalData::class.java.simpleName} or situated after all modal data parameters.
//All modal data must be inserted after the event, with the same order as the constructed modal, before inserting modal inputs and custom parameters"""
//            }
//
//            val data = userData[i]
//            requireUser(parameter.boxedType.jvmErasure.isSuperclassOf(data::class)) {
//                "The modal user data '%s' is not a valid type (expected a %s, got a %s)".format(
//                    parameter.parameter.name,
//                    parameter.boxedType.simpleName,
//                    data.javaClass.simpleName
//                )
//            }
//
//            objects.add(data)
//        }
//        for (parameter in parameters) {
//            if (parameter.isModalData) continue  //We already processed modal data
//            val obj: Any?
//            if (parameter.isOption && parameter.isModalInput) {
//                //We have the modal input's ID
//                // But we have a Map of input *name* -> InputData (contains input ID)
//                val inputId = inputNameToInputIdMap[parameter.modalInputName]
//                    ?: throwUser("Modal input named '${parameter.modalInputName}' was not found")
//                val modalMapping = event.getValue(inputId)
//                    ?: throwUser("Modal input ID '$inputId' was not found on the event")
//                obj = parameter.resolver.resolve(context, this, event, modalMapping)
//
//                requireUser(obj != null) {
//                    "The parameter '${parameter.parameter.name}' of value '${modalMapping.asString}' could not be resolved into a ${parameter.boxedType.simpleName}"
//                }
//
//                requireUser(parameter.boxedType.jvmErasure.isSuperclassOf(obj::class)) {
//                    "The parameter '${parameter.parameter.name}' of value '${modalMapping.asString}' is not a valid type (expected a ${parameter.boxedType.simpleName})"
//                }
//            } else {
//                obj = parameter.customResolver.resolve(context, this, event)
//            }
//
//            //For some reason using an array list instead of a regular array
//            // magically unboxes primitives when passed to Method#invoke
//            objects.add(obj)
//        }
//
//        try {
//            method.call(*objects.toTypedArray())
//        } catch (e: Throwable) {
//            throwableConsumer.accept(e)
//        }
//
//        return true
    }
}