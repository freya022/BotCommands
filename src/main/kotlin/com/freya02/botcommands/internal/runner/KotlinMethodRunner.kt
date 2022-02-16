package com.freya02.botcommands.internal.runner

import com.freya02.botcommands.internal.ConsumerEx
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

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    override fun <T : Any?> invoke(args: Array<out Any>, throwableConsumer: Consumer<Throwable>, successCallback: ConsumerEx<T>?) {
        if (kFunction != null && isSuspend == true) {
            scope.launch(dispatcher) {
                try {
                    val returnValue = kFunction.callSuspend(instance, *args)
                    successCallback?.accept(returnValue as T)
                } catch (e: Throwable) {
                    throwableConsumer.accept(e)
                }
            }
        } else {
            //No need for a thread pool or exception handling, we're already in a thread wrapped by a try catch
            val returnValue = method.invoke(instance, *args)
            successCallback?.accept(returnValue as T) //Running it is optional
        }
    }
}