package dev.freya02.botcommands.method.accessors

import dev.freya02.botcommands.method.accessors.internal.ClassFileMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.KotlinReflectMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.MethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.exceptions.IllegalSuspendCallException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KFunction
import kotlin.time.Duration.Companion.milliseconds

object MethodAccessorTest {

    @MethodSource("testCallers")
    @ParameterizedTest
    fun `Generate ClassFile method accessors and call them`(instance: Any?, function: KFunction<*>, args: List<Any?>) {
        runBlocking {
            val methodAccessor = ClassFileMethodAccessorFactory().create(instance, function)
            val args = methodAccessor.createBlankArguments().also {
                args.forEach { arg -> it.push(arg) }
            }

            methodAccessor.callSuspend(args)
        }
    }

    @MethodSource("testCallers")
    @ParameterizedTest
    fun `Generate kotlin-reflect method accessors and call them`(instance: Any?, function: KFunction<*>, args: List<Any?>) {
        runBlocking {
            val methodAccessor = KotlinReflectMethodAccessorFactory().create(instance, function)
            val args = methodAccessor.createBlankArguments().also {
                args.forEach { arg -> it.push(arg) }
            }

            methodAccessor.callSuspend(args)
        }
    }

    @JvmStatic
    fun testCallers(): List<Arguments> = listOf(
        Arguments.argumentSet("0-arg method", TestClass(), TestClass::run, listOf<Any?>()),
        Arguments.argumentSet("1-arg method", TestClass(), TestClass::runWithArgs, listOf<Any?>("foobar")),
        Arguments.argumentSet("1-arg constructor", null, ::TestConstructor, listOf<Any?>(1)),
        Arguments.argumentSet("Constructor with defaults", null, ::TestConstructorWithDefaults, listOf<Any?>()),
        Arguments.argumentSet("Unboxing", TestClass(), TestClass::runWithUnboxing, listOf<Any?>(true, 1.toByte(), 1.toChar(), 1.toShort(), 1, 1.toLong(), 1.toFloat(), 1.toDouble())),
        Arguments.argumentSet("From interface", object : TestInterface {}, TestInterface::run, listOf<Any?>()),
        Arguments.argumentSet("With return type", TestClass(), TestClass::runWithReturnType, listOf<Any?>()),
        Arguments.argumentSet("With static modifier", null, TestStatic::run, listOf<Any?>()),
        Arguments.argumentSet("With static modifier and instance", TestStatic, TestStatic::run, listOf<Any?>()),
        Arguments.argumentSet("With defaults", TestClass(), TestClass::runWithDefaults, listOf<Any?>()),
        Arguments.argumentSet("With more defaults", TestClass(), TestClass::runWithMoreDefaults, listOf<Any?>("foobar")),
        Arguments.argumentSet("With overridden defaults", TestClass(), TestClass::runWithDefaults, listOf<Any?>(3)),
        Arguments.argumentSet("With optional parameter set to null", TestClass(), TestClass::runWithNullOptional, listOf<Any?>(null)),
        Arguments.argumentSet("0-arg suspend method", TestClass(), TestClass::coRun, listOf<Any?>()),
        Arguments.argumentSet("Suspend with defaults", TestClass(), TestClass::coRunWithDefaults, listOf<Any?>()),
        Arguments.argumentSet("Suspend with overridden defaults", TestClass(), TestClass::coRunWithDefaults, listOf<Any?>(3)),
        Arguments.argumentSet("Suspend with suspension points", TestClass(), TestClass::coRunWithSuspensionPoints, listOf<Any?>(1, 1)),
    )

    @MethodSource("factories")
    @ParameterizedTest
    fun `'call' throws on non-suspend functions`(factory: MethodAccessorFactory) {
        assertThrows<IllegalSuspendCallException> {
            val instance = TestClass()
            val function = TestClass::coRun
            val methodAccessor = factory.create(instance, function)
            methodAccessor.call(methodAccessor.createBlankArguments())
        }

        assertDoesNotThrow {
            val instance = TestClass()
            val function = TestClass::run
            val methodAccessor = factory.create(instance, function)
            methodAccessor.call(methodAccessor.createBlankArguments())
        }
    }

    @JvmStatic
    fun factories() = listOf(
        Arguments.argumentSet("ClassFile", ClassFileMethodAccessorFactory()),
        Arguments.argumentSet("kotlin-reflect", KotlinReflectMethodAccessorFactory()),
    )
}

interface TestInterface {

    fun run() {

    }
}

object TestStatic {

    @JvmStatic
    fun run() {

    }
}

class TestConstructor(arg: Int)

class TestConstructorWithDefaults(arg: Int = 2)

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
