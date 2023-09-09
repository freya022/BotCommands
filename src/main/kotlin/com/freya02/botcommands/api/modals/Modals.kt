package com.freya02.botcommands.api.modals

import com.freya02.botcommands.api.modals.annotations.ModalHandler
import com.freya02.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

fun Modals.create(title: String, block: context(Modals) ModalBuilder.() -> Unit) =
    create(title).apply { block(this@create, this) }.build()

context(Modals)
fun ModalBuilder.textInput(inputName: String, label: String, inputStyle: TextInputStyle, block: TextInputBuilder.() -> Unit = {}) {
    addActionRow(createTextInput(inputName, label, inputStyle).apply(block).build())
}

context(Modals)
fun ModalBuilder.shortTextInput(inputName: String, label: String, block: TextInputBuilder.() -> Unit = {}) {
    textInput(inputName, label, TextInputStyle.SHORT, block)
}

context(Modals)
fun ModalBuilder.paragraphTextInput(inputName: String, label: String, block: TextInputBuilder.() -> Unit = {}) {
    textInput(inputName, label, TextInputStyle.PARAGRAPH, block)
}

fun ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent) -> Unit) {
    bindToCallable(func as KFunction<*>, emptyList())
}

fun <T1> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1) -> Unit, arg1: T1) {
    bindToCallable(func as KFunction<*>, listOf<Any?>(arg1))
}

fun <T1, T2> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2) -> Unit, arg1: T1, arg2: T2) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2))
}

fun <T1, T2, T3> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2, T3) -> Unit, arg1: T1, arg2: T2, arg3: T3) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3))
}

fun <T1, T2, T3, T4> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2, T3, T4) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4))
}

fun <T1, T2, T3, T4, T5> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2, T3, T4, T5) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5))
}

fun <T1, T2, T3, T4, T5, T6> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2, T3, T4, T5, T6) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

fun <T1, T2, T3, T4, T5, T6, T7> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2, T3, T4, T5, T6, T7) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

fun <T1, T2, T3, T4, T5, T6, T7, T8> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2, T3, T4, T5, T6, T7, T8) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10) {
    bindToCallable(func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

@PublishedApi
internal fun ModalBuilder.bindToCallable(func: KFunction<*>, data: List<Any?>) {
    val name = findHandlerName(func)
        ?: throwUser(func, "Could not find @${ModalHandler::class.simpleName}")
    this.bindTo(name, data)
}

private fun findHandlerName(func: KFunction<*>): String? {
    return func.findAnnotation<ModalHandler>()?.name
}