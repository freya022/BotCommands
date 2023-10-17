package io.github.freya022.botcommands.othertests

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure

object FactoryTypeTest {
    abstract class AnyResolverList<E : ParameterResolver<*, *>> : List<E>
    abstract class ClassResolverList : AnyResolverList<ClassParameterResolver<*, *>>()

    @JvmStatic
    fun main(args: Array<String>) {
        val clazz = AnyResolverList::class
        val factoryType = clazz.allSupertypes.first { it.jvmErasure == List::class }

        val deepClazz = ClassResolverList::class
        val deepFactoryType = deepClazz.allSupertypes.first { it.jvmErasure == List::class }

        println("Any type of resolver: ${factoryType.arguments[0].type!!.simpleNestedName}")
        println("Class resolver: ${deepFactoryType.arguments[0].type!!.simpleNestedName}")
    }
}