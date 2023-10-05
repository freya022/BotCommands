package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.utils.isStatic
import io.github.freya022.botcommands.api.core.utils.isSubclassOfAny
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.jvmErasure

private typealias Function = KFunction<*>
private typealias FunctionIterable = Iterable<Function>

internal abstract class FunctionFilter {
    abstract val errorMessage: String

    protected abstract fun filter(function: Function): Boolean

    operator fun invoke(function: Function, required: Boolean): Boolean {
        return filter(function).also { ok ->
            if (!ok && required)
                throwUser(function, errorMessage)
        }
    }

    companion object {
        private fun Function.checkReturnType(types: Array<out KClass<*>>): Boolean = returnType.jvmErasure.isSubclassOfAny(*types)

        private fun hasFirstArg(it: KFunction<*>, types: Array<out KClass<*>>) =
            when (val firstParam = it.nonInstanceParameters.firstOrNull()) {
                null -> false
                else -> firstParam.type.jvmErasure.isSubclassOfAny(*types)
            }

        private fun Array<out KClass<*>>.toTypesArrayString() =
            joinToString(prefix = "[", postfix = "]") { type -> type.simpleNestedName }

        fun returnType(vararg types: KClass<*>) = object : FunctionFilter() {
            override val errorMessage: String
                get() = "Function must return any a superclass of: ${types.toTypesArrayString()}"

            override fun filter(function: Function): Boolean = function.checkReturnType(types)
        }

        fun firstArg(vararg types: KClass<*>) = object : FunctionFilter() {
            override val errorMessage: String
                get() = "Function must have a first parameter with a superclass of: ${types.toTypesArrayString()}"

            override fun filter(function: Function): Boolean = hasFirstArg(function, types)
        }

        fun static() = object : FunctionFilter() {
            override val errorMessage: String
                get() = "Function must be static"

            override fun filter(function: Function): Boolean = function.isStatic
        }

        fun staticOrCompanion() = object : FunctionFilter() {
            override val errorMessage: String
                get() = "Function must be static"

            override fun filter(function: Function): Boolean = function.isStatic || (function.instanceParameter?.type?.jvmErasure?.isCompanion ?: false)
        }

        fun nonStatic() = object : FunctionFilter() {
            override val errorMessage: String
                get() = "Function must not be static"

            override fun filter(function: Function): Boolean = !function.isStatic
        }

        fun blocking() = object : FunctionFilter() {
            override val errorMessage: String
                get() = "Function must not be suspending"

            override fun filter(function: Function): Boolean = !function.isSuspend
        }

        inline fun <reified A : Annotation> annotation() = object : FunctionFilter() {
            override val errorMessage: String
                get() = "Function must be annotated with @${A::class.simpleNestedName}"

            override fun filter(function: Function): Boolean = function.hasAnnotation<A>()
        }
    }
}

internal fun <C : FunctionIterable> C.withFilter(filter: FunctionFilter) = this.filter { filter(it, false) }
internal fun <C : FunctionIterable> C.requiredFilter(filter: FunctionFilter) = this.onEach { filter(it, true) }