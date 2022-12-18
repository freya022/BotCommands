package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.commands.annotations.Optional
import com.freya02.botcommands.internal.annotations.IncludeClasspath
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.isService
import io.github.classgraph.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

private typealias IsNullableAnnotated = Boolean

internal object ReflectionMetadata {
    internal class KFunctionMetadata(val line: Int, val nullabilities: List<IsNullableAnnotated>)

    private var scannedParams: Boolean = false

    private val functionMetadataMap_: MutableMap<Method, KFunctionMetadata> = hashMapOf()
    private val functionMetadataMap: Map<Method, KFunctionMetadata> by lazy {
        if (!scannedParams)
            throwInternal("Tried to access a function metadata but they haven't been scanned yet")

        Collections.unmodifiableMap(functionMetadataMap_)
    }

    internal fun runScan(packages: Collection<String>, userClasses: Collection<Class<*>>): List<Class<*>> {
        val scanned: List<Pair<ScanResult, ClassInfoList>> = Benchmark.printTimings("ClassGraph") { buildList {
            ClassGraph()
                .acceptPackages("com.freya02.botcommands")
                .enableMethodInfo()
                .enableAnnotationInfo()
                .scan()
                .also { scanResult -> // Don't keep test classes
                    add(scanResult to scanResult.allStandardClasses.filter {
                        if (it.packageName.startsWith("com.freya02.botcommands.test")) {
                            return@filter false
                        } else {
                            return@filter it.isService()
                                    || it.outerClasses.any { outer -> outer.isService() }
                                    || it.hasAnnotation(IncludeClasspath::class.java.name)
                        }
                    })
                }

            ClassGraph()
                .acceptPackages(*packages.toTypedArray())
                .acceptClasses(*userClasses.map { it.simpleName }.toTypedArray())
                .enableMethodInfo()
                .enableAnnotationInfo()
                .scan()
                .also { scanResult ->
                    add(scanResult to scanResult.allStandardClasses)
                }
            }
        }

        return Benchmark.printTimings("Process") { scanned.flatMap { (scanResult, classes) ->
            classes
                .asSequence()
                .filter {
                    it.annotationInfo.directOnly()["kotlin.Metadata"]?.let { annotationInfo ->
                        return@filter KotlinClassHeader.Kind.getById(annotationInfo.parameterValues["k"].value as Int) == KotlinClassHeader.Kind.CLASS
                    }
                    return@filter true
                }
                .filter { !(it.isInnerClass || it.isSynthetic || it.isEnum || it.isAbstract) }
                .filter(ReflectionUtilsKt::isInstantiable)
                .toList()
                .also { readAnnotations(it) }
                .map { it.loadClass() }
                .also {
                    scanResult.close()
                }
        } }
    }


    private fun readAnnotations(classInfoList: List<ClassInfo>) {
        for (classInfo in classInfoList) {
            try {
                for (methodInfo in classInfo.declaredMethodInfo) {
                    //Don't inspect methods with generics
                    if (methodInfo.parameterInfo.any { it.typeSignatureOrTypeDescriptor is TypeVariableSignature || it.typeSignatureOrTypeDescriptor is ArrayTypeSignature }) continue

                    val method = methodInfo.loadClassAndGetMethod()
                    val nullabilities = methodInfo.parameterInfo.dropLast(if (method.isSuspend) 1 else 0).map { parameterInfo ->
                        parameterInfo.annotationInfo.any { it.name.endsWith("Nullable") }
                                || parameterInfo.hasAnnotation(Optional::class.java)
                    }

                    functionMetadataMap_[method] = KFunctionMetadata(methodInfo.minLineNum, nullabilities)
                }
            } catch (e: Throwable) {
                throw RuntimeException("An exception occurred while scanning class: ${classInfo.name}", e)
            }
        }

        scannedParams = true
    }

    internal val KParameter.isNullable: Boolean
        get() {
            val metadata = functionMetadataMap[function.javaMethod]
                ?: throwUser("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")
            val isNullableAnnotated = metadata.nullabilities[index - 1] // -1 because 0 is actually the instance parameter
            val isNullableMarked = type.isMarkedNullable

            return isNullableAnnotated || isNullableMarked
        }

    private val handle: MethodHandle by lazy {
        val kParameterImplClazz = Class.forName("kotlin.reflect.jvm.internal.KParameterImpl")
        MethodHandles.publicLookup().unreflect(kParameterImplClazz.kotlin.memberProperties.find { it.name == "callable" }!!.javaGetter!!)
    }
    internal val KParameter.function: KFunction<*>
        get() {
            val callable = handle.invoke(this)
            return callable as? KFunction<*>
                ?: throwInternal("Unable to get the function of a KParameter, callable is: $callable")
        }

    internal val KFunction<*>.isJava
        get() = javaMethod?.declaringClass?.isAnnotationPresent(Metadata::class.java)?.not() ?: false //If there's no java method then it's def not java ?

    internal val KFunction<*>.lineNumber: Int
        get() = (functionMetadataMap[this.javaMethod]
            ?: throwUser("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")).line

    private val Method.isSuspend: Boolean
        get() = parameters.any { it.type == Continuation::class.java }
}