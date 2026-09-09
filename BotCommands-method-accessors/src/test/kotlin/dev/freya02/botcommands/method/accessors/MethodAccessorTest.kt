package dev.freya02.botcommands.method.accessors

import dev.freya02.botcommands.method.accessors.internal.ClassFileMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.KotlinReflectMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.MethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.exceptions.IllegalSuspendCallException
import kotlinx.coroutines.delay
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KFunction
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

object MethodAccessorTest {

    @MethodSource("testCallers")
    @ParameterizedTest
    suspend fun `Generate ClassFile method accessors and call them`(instance: Any?, function: KFunction<*>, args: List<Any?>) {
        val methodAccessor = ClassFileMethodAccessorFactory().create(instance, function)
        val args = methodAccessor.createBlankArguments().also {
            args.forEach { arg -> it.push(arg) }
        }

        methodAccessor.callSuspend(args)
    }

    @MethodSource("testCallers")
    @ParameterizedTest
    suspend fun `Generate kotlin-reflect method accessors and call them`(instance: Any?, function: KFunction<*>, args: List<Any?>) {
        val methodAccessor = KotlinReflectMethodAccessorFactory().create(instance, function)
        val args = methodAccessor.createBlankArguments().also {
            args.forEach { arg -> it.push(arg) }
        }

        methodAccessor.callSuspend(args)
    }

    @JvmStatic
    fun testCallers(): List<Arguments> = listOf(
        testCaller("0-arg method", TestClass(), TestClass::run, listOf()),
        testCaller("1-arg method", TestClass(), TestClass::runWithArgs, listOf<Any?>("foobar")),
        testCaller("1-arg constructor", null, ::TestConstructor, listOf<Any?>(1)),
        testCaller("Inline class arg", TestClass(), TestClass::runWithInlineClassArg, listOf<Any?>(InlineDouble(3.14159))),
        testCaller("Nested inline class arg", TestClass(), TestClass::runWithNestedInlineClassArg, listOf<Any?>(NestedInlineDouble(InlineDouble(3.14159)))),
        testCaller("Constructor with defaults", null, ::TestConstructorWithDefaults, listOf()),
        testCaller("Unboxing", TestClass(), TestClass::runWithUnboxing, listOf<Any?>(true, 1.toByte(), 1.toChar(), 1.toShort(), 1, 1.toLong(), 1.toFloat(), 1.toDouble())),
        testCaller("From interface", object : TestInterface {}, TestInterface::run, listOf()),
        testCaller("With return type", TestClass(), TestClass::runWithReturnType, listOf()),
        testCaller("With inline class return type", TestClass(), TestClass::runWithInlineClassReturnType, listOf()),
        testCaller("With nested inline class return type", TestClass(), TestClass::runWithNestedInlineClassReturnType, listOf()),
        testCaller("With static modifier", null, TestStatic::run, listOf()),
        testCaller("From object", TestObject, TestObject::run, listOf()),
        testCaller("Nested class constructor 0-arg", null, TestClass::NestedClass, listOf()),
        testCaller("Inner class constructor 0-arg", TestClass(), TestClass::InnerClass, listOf()),
        testCaller("Inner class constructor 1-arg", TestClass(), TestClass::InnerClassOneArg, listOf("foobar")),
        testCaller("Inner class constructor with defaults", TestClass(), TestClass::InnerClassWithDefaults, listOf()),
        testCaller("With static modifier and instance", TestStatic, TestStatic::run, listOf()),
        testCaller("With defaults", TestClass(), TestClass::runWithDefaults, listOf()),
        testCaller("With inline class default", TestClass(), TestClass::runWithDefaultInlineClassArg, listOf()),
        testCaller("With nested inline class default", TestClass(), TestClass::runWithDefaultNestedInlineClassArg, listOf()),
        testCaller("With more defaults", TestClass(), TestClass::runWithMoreDefaults, listOf<Any?>("foobar")),
        testCaller("With overridden defaults", TestClass(), TestClass::runWithDefaults, listOf<Any?>(3)),
        testCaller("With optional parameter set to null", TestClass(), TestClass::runWithNullOptional, listOf<Any?>(null)),

        testCaller("0-arg suspend method", TestClass(), TestClass::coRun, listOf()),
        testCaller("Suspend with defaults", TestClass(), TestClass::coRunWithDefaults, listOf()),
        testCaller("Suspend with overridden defaults", TestClass(), TestClass::coRunWithDefaults, listOf<Any?>(3)),
        testCaller("Suspend with suspension points", TestClass(), TestClass::coRunWithSuspensionPoints, listOf<Any?>(1, 1)),
        testCaller("Suspend with inline class arg", TestClass(), TestClass::coRunWithInlineClassArg, listOf<Any?>(InlineDouble(3.14159))),
        testCaller("Suspend with nested inline class arg", TestClass(), TestClass::coRunWithNestedInlineClassArg, listOf<Any?>(NestedInlineDouble(InlineDouble(3.14159)))),
        testCaller("Suspend and return inline class", TestClass(), TestClass::coRunWithInlineClassReturnType, listOf()),
    )

    private fun testCaller(name: String, instance: Any?, function: KFunction<*>, arguments: List<Any?>): Arguments {
        return Arguments.argumentSet(name, instance, function, arguments)
    }

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

    @MethodSource("factories")
    @ParameterizedTest
    fun `Return type of inline class returns an inline class instance`(factory: MethodAccessorFactory) {
        val accessor = factory.create(TestClass(), TestClass::runWithInlineClassReturnType as KFunction<*>)
        val result = accessor.call(accessor.createBlankArguments())
        assertIs<InlineDouble>(result)
    }

    @MethodSource("factories")
    @ParameterizedTest
    suspend fun `Coroutine return type of inline class returns an inline class instance`(factory: MethodAccessorFactory) {
        val accessor = factory.create(TestClass(), TestClass::coRunWithInlineClassReturnType as KFunction<*>)
        val result = accessor.callSuspend(accessor.createBlankArguments())
        assertIs<InlineDouble>(result)
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

object TestObject {

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

    fun runWithInlineClassArg(arg: InlineDouble) {

    }

    fun runWithNestedInlineClassArg(arg: NestedInlineDouble) {

    }

    fun runWithDefaultInlineClassArg(arg: InlineDouble = InlineDouble(2.0)) {

    }

    fun runWithDefaultNestedInlineClassArg(arg: NestedInlineDouble = NestedInlineDouble(InlineDouble(2.0))) {

    }

    fun runWithInlineClassReturnType(): InlineDouble {
        return InlineDouble(2.0)
    }

    fun runWithNestedInlineClassReturnType(): NestedInlineDouble {
        return NestedInlineDouble(InlineDouble(2.0))
    }

    suspend fun coRun() {
        delay(10.milliseconds)
    }

    suspend fun coRunWithDefaults(int: Int = 2): Int {
        delay(10.milliseconds)
        return int
    }

    suspend fun coRunWithSuspensionPoints(a: Int, b: Int): Int {
        delay(10.milliseconds)
        val c = a + b
        delay(10.milliseconds)
        println("$a + $b = $c")
        return c
    }

    suspend fun coRunWithInlineClassArg(arg: InlineDouble) {

    }

    suspend fun coRunWithNestedInlineClassArg(arg: NestedInlineDouble) {

    }

    suspend fun coRunWithInlineClassReturnType(): InlineDouble {
        return InlineDouble(2.0)
    }

    class NestedClass

    @Suppress("RedundantInnerClassModifier")
    inner class InnerClass

    @Suppress("RedundantInnerClassModifier")
    inner class InnerClassOneArg(@Suppress("unused") arg: String)

    @Suppress("RedundantInnerClassModifier")
    inner class InnerClassWithDefaults(@Suppress("unused") val x: Int = 1)
}

@JvmInline
value class NestedInlineDouble(val value: InlineDouble)

@JvmInline
value class InlineDouble(val value: Double)
