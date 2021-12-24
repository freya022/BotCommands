package com.freya02.botcommands.internal.runner

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.reflect.Method
import java.util.function.Consumer
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

class KotlinMethodRunner(private val instance: Any,
                         private val method: Method,
                         private val scope: CoroutineScope,
                         private val dispatcher: CoroutineDispatcher) : MethodRunner {
    private val kFunction: KFunction<*>? = method.kotlinFunction
    private val isSuspend: Boolean? = kFunction?.isSuspend

    @Throws(Exception::class)
    override fun invoke(args: Array<out Any>, throwableConsumer: Consumer<Throwable>) {
        if (kFunction != null && isSuspend == true) {
            scope.launch(dispatcher) {
                try {
                    kFunction.callSuspend(instance, *args)
                } catch (e: Throwable) {
                    throwableConsumer.accept(e)
                }
            }
        } else {
            //No need for a thread pool or exception handling, we're already in a thread wrapped by a try catch
            method.invoke(instance, *args)
        }
    }
}