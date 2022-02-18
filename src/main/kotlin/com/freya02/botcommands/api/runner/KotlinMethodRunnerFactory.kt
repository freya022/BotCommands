package com.freya02.botcommands.api.runner

import com.freya02.botcommands.internal.runner.KotlinMethodRunner
import com.freya02.botcommands.internal.runner.MethodRunner
import com.freya02.botcommands.internal.runner.MethodRunnerFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.lang.reflect.Method
import kotlin.reflect.jvm.kotlinFunction

class KotlinMethodRunnerFactory(private val dispatcher: CoroutineDispatcher, private val scope: CoroutineScope) : MethodRunnerFactory() {
    override fun make(instance: Any, method: Method): MethodRunner = KotlinMethodRunner(instance, method, scope, dispatcher)

    override fun supportsSuspend(): Boolean = true

    override fun isSuspend(method: Method): Boolean {
        val kFunction = method.kotlinFunction

        return kFunction != null && kFunction.isSuspend
    }
}