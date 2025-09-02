package dev.freya02.botcommands.method.accessors

import dev.freya02.botcommands.method.accessors.internal.ClassFileMethodAccessorFactory
import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

// TODO move this to BotCommands-method-accessors
@Suppress("FunctionName")
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
open class MethodAccessorBenchmark {

    private lateinit var instance: MyClass

    private lateinit var simpleMethodAccessorClassFile: MethodAccessor<*>
    private lateinit var methodWithDefaultsAccessorClassFile: MethodAccessor<*>
    private lateinit var suspendingMethodWithDefaultsAccessorClassFile: MethodAccessor<*>

    private lateinit var simpleMethodKotlin: KFunction<*>
    private lateinit var methodWithDefaultsKotlin: KFunction<*>
    private lateinit var suspendingMethodWithDefaultsKotlin: KFunction<*>

    @Param("foobar")
    lateinit var sampleString: String

    @Setup
    fun setup() {
        instance = MyClass()

        // The generated accessors won't matter as the benchmark calls 'callSuspendBy' and not 'invoke'
        simpleMethodKotlin = MyClass::simpleMethod
        methodWithDefaultsKotlin = MyClass::methodWithDefaults
        suspendingMethodWithDefaultsKotlin = MyClass::suspendingMethodWithDefaults

        simpleMethodAccessorClassFile = ClassFileMethodAccessorFactory().create(instance, MyClass::simpleMethod)
        methodWithDefaultsAccessorClassFile = ClassFileMethodAccessorFactory().create(instance, MyClass::methodWithDefaults)
        suspendingMethodWithDefaultsAccessorClassFile = ClassFileMethodAccessorFactory().create(instance, MyClass::suspendingMethodWithDefaults)
    }

    @Benchmark
    fun simpleMethod_Baseline(): String = runBlocking {
        instance.simpleMethod()
    }

    @Benchmark
    fun methodWithDefaults_Baseline(): String = runBlocking {
        instance.methodWithDefaults(sampleString)
    }

    @Benchmark
    fun suspendingMethodWithDefaults_Baseline(): String = runBlocking {
        instance.suspendingMethodWithDefaults(sampleString)
    }

    @Benchmark
    fun simpleMethod_Accessor_ClassFile(): String = runBlocking {
        val args = simpleMethodAccessorClassFile.createBlankArguments()
        simpleMethodAccessorClassFile.callSuspend(args) as String
    }

    @Benchmark
    fun methodWithDefaults_Accessor_ClassFile(): String = runBlocking {
        val args = methodWithDefaultsAccessorClassFile.createBlankArguments()
        args[0] = sampleString
        methodWithDefaultsAccessorClassFile.callSuspend(args) as String
    }

    @Benchmark
    fun suspendingMethodWithDefaults_Accessor_ClassFile(): String = runBlocking {
        val args = suspendingMethodWithDefaultsAccessorClassFile.createBlankArguments()
        args[0] = sampleString
        suspendingMethodWithDefaultsAccessorClassFile.callSuspend(args) as String
    }

    @Benchmark
    fun simpleMethod_Kotlin(): String = runBlocking {
        val function = simpleMethodKotlin
        function.callSuspendBy(mapOf(function.instanceParameter!! to instance)) as String
    }

    @Benchmark
    fun methodWithDefaults_Kotlin(): String = runBlocking {
        val function = methodWithDefaultsKotlin
        function.callSuspendBy(mapOf(function.instanceParameter!! to instance, function.valueParameters[0] to sampleString)) as String
    }

    @Benchmark
    fun suspendingMethodWithDefaults_Kotlin(): String = runBlocking {
        val function = suspendingMethodWithDefaultsKotlin
        function.callSuspendBy(mapOf(function.instanceParameter!! to instance, function.valueParameters[0] to sampleString)) as String
    }

    class MyClass {

        fun simpleMethod(): String {
            return "abc"
        }

        fun methodWithDefaults(a: String, b: Int = 42, c: Double = 3.14159): String {
            return "$a$b$c"
        }

        suspend fun suspendingMethodWithDefaults(a: String, b: Int = 42, c: Double = 3.14159): String {
            return "$a$b$c"
        }
    }
}
