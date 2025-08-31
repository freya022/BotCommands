package dev.freya02.botcommands.method.accessors

import dev.freya02.botcommands.method.accessors.internal.ClassFileMethodAccessorFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.time.Duration.Companion.milliseconds

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

    fun runWithMoreDefaults(a: String, b: Int = 42, c: Double = 3.14159) {

    }

    fun runWithNullOptional(arg: Int? = 2) {
        require(arg == null) { "The expected argument was null but the accessor replaced it with a value ($arg)" }
    }

    fun runWithReturnType(): Int {
        return 1
    }

    fun runWithReturnTypeWithDefaults(arg: Int = 2): Int {
        return arg
    }

    suspend fun coRun() {

    }

    suspend fun coRunWithDefaults(int: Int = 2) {

    }

    suspend fun coRunWithSuspensionPoints(a: Int, b: Int): Int {
        delay(10.milliseconds)
        val c = a + b
        delay(10.milliseconds)
        println("$a + $b = $c")
        return c
    }
}

object ClassFileMethodAccessorGeneratorTest {

    @MethodSource("testCallers")
    @ParameterizedTest
    fun `Generate method accessors and call them`(instance: Any?, function: KFunction<*>, args: List<Any?>) {
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
        argumentSet("With static modifier", null, TestStatic::run, listOf<Any?>()),
        argumentSet("With static modifier and instance", TestStatic, TestStatic::run, listOf<Any?>()),
        argumentSet("With defaults", TestClass(), TestClass::runWithDefaults, listOf<Any?>()),
        argumentSet("With more defaults", TestClass(), TestClass::runWithMoreDefaults, listOf<Any?>("foobar")),
        argumentSet("With overridden defaults", TestClass(), TestClass::runWithDefaults, listOf<Any?>(3)),
        argumentSet("With optional parameter set to null", TestClass(), TestClass::runWithNullOptional, listOf<Any?>(null)),

        argumentSet("0-arg suspend method", TestClass(), TestClass::coRun, listOf<Any?>()),
        argumentSet("Suspend with defaults", TestClass(), TestClass::coRunWithDefaults, listOf<Any?>()),
        argumentSet("Suspend with overridden defaults", TestClass(), TestClass::coRunWithDefaults, listOf<Any?>(3)),
        argumentSet("Suspend with suspension points", TestClass(), TestClass::coRunWithSuspensionPoints, listOf<Any?>(1, 1)),
    )
}
