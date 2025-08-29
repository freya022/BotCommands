package dev.freya02.botcommands.method.accessors

import dev.freya02.botcommands.method.accessors.internal.ClassFileMethodAccessorFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

interface TestInterface {

    fun run() {

    }
}

object TestStatic {

    @JvmStatic
    fun run() {

    }
}

class TestConstructor(arg: Int = 2)

class TestClass {

    fun run() {

    }

    fun runWithArgs(arg: String) {

    }

    fun runWithUnboxing(boolean: Boolean, byte: Byte, char: Char, short: Short, int: Int, long: Long, float: Float, double: Double) {

    }

    fun runWithDefaults(arg: Int = 2) {

    }

    fun runWithReturnType(): Int {
        return 1
    }

    fun runWithReturnTypeWithDefaults(arg: Int = 2): Int {
        return arg
    }
}

object ClassFileMethodAccessorGeneratorTest {

    @MethodSource("testCallers")
    @ParameterizedTest
    fun `Generate method accessors and call them`(instance: Any, function: KFunction<*>, args: List<Any?>) {
        runBlocking {
            val methodAccessor = ClassFileMethodAccessorFactory().create(instance, function)
            methodAccessor.call(buildMap {
                args.forEachIndexed { index, arg ->
                    this[function.valueParameters[index]] = arg
                }
            })
        }
    }

    @JvmStatic
    fun testCallers(): List<Arguments> = listOf(
        argumentSet("0-arg method", TestClass(), TestClass::run, listOf<Any?>()),
        argumentSet("1-arg method", TestClass(), TestClass::runWithArgs, listOf<Any?>("foobar")),
        argumentSet("Unboxing", TestClass(), TestClass::runWithUnboxing, listOf<Any?>(true, 1.toByte(), 1.toChar(), 1.toShort(), 1, 1.toLong(), 1.toFloat(), 1.toDouble())),
        argumentSet("From interface", object : TestInterface { }, TestInterface::run, listOf<Any?>()),
        argumentSet("With return type", TestClass(), TestClass::runWithReturnType, listOf<Any?>()),
    )
}
