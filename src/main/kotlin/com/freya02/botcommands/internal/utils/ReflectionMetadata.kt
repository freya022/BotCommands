package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.commands.annotations.Optional
import com.freya02.botcommands.internal.annotations.IncludeClasspath
import com.freya02.botcommands.internal.javaMethodOrConstructor
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils.isService
import io.github.classgraph.*
import java.lang.reflect.Executable
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader

private typealias IsNullableAnnotated = Boolean

internal object ReflectionMetadata {
    private class ClassMetadata(val sourceFile: String)
    private class MethodMetadata(val line: Int, val nullabilities: List<IsNullableAnnotated>)

    private var scannedParams: Boolean = false

    private val classMetadataMap_: MutableMap<Class<*>, ClassMetadata> = hashMapOf()
    private val classMetadataMap: Map<Class<*>, ClassMetadata> by lazy {
        if (!scannedParams)
            throwInternal("Tried to access class metadata but they haven't been scanned yet")

        Collections.unmodifiableMap(classMetadataMap_)
    }

    private val methodMetadataMap_: MutableMap<Executable, MethodMetadata> = hashMapOf()
    private val methodMetadataMap: Map<Executable, MethodMetadata> by lazy {
        if (!scannedParams)
            throwInternal("Tried to access method metadata but they haven't been scanned yet")

        Collections.unmodifiableMap(methodMetadataMap_)
    }

    internal fun runScan(packages: Collection<String>, userClasses: Collection<Class<*>>): List<Class<*>> {
        val scanned: List<Pair<ScanResult, ClassInfoList>> = buildList {
            ClassGraph()
                .acceptPackages("com.freya02.botcommands.api", "com.freya02.botcommands.internal")
                .enableMethodInfo()
                .enableAnnotationInfo()
                .disableModuleScanning()
                .disableNestedJarScanning()
                .scan()
                .also { scanResult -> // Don't keep test classes
                    add(scanResult to scanResult.allStandardClasses.filter {
                        return@filter it.isService()
                                || it.outerClasses.any { outer -> outer.isService() }
                                || it.hasAnnotation(IncludeClasspath::class.java.name)
                    })
                }

            ClassGraph()
                .acceptPackages(*packages.toTypedArray())
                .acceptClasses(*userClasses.map { it.name }.toTypedArray())
                .enableMethodInfo()
                .enableAnnotationInfo()
                .disableModuleScanning()
                .disableNestedJarScanning()
                .scan()
                .also { scanResult ->
                    add(scanResult to scanResult.allStandardClasses)
                }
        }

        return scanned.flatMap { (scanResult, classes) ->
            classes
                .asSequence()
                .filter {
                    it.annotationInfo.directOnly()["kotlin.Metadata"]?.let { annotationInfo ->
                        return@filter KotlinClassHeader.Kind.getById(annotationInfo.parameterValues["k"].value as Int) == KotlinClassHeader.Kind.CLASS
                    }
                    return@filter true
                }
                .filterNot { it.name.contains("\$special") }
                .filterNot { it.isSynthetic || it.isEnum || it.isAbstract } //Don't keep these
                .filter(ReflectionUtils::isInstantiable)
                .toList()
                .also { readAnnotations(it) }
                .map { it.loadClass() }
                .also {
                    scanResult.close()
                }
        }
    }


    private fun readAnnotations(classInfoList: List<ClassInfo>) {
        for (classInfo in classInfoList) {
            try {
                for (methodInfo in classInfo.declaredMethodAndConstructorInfo) {
                    //Don't inspect methods with generics
                    if (methodInfo.parameterInfo
                            .map { it.typeSignatureOrTypeDescriptor }
                            .any { it is TypeVariableSignature || (it is ArrayTypeSignature && it.elementTypeSignature is TypeVariableSignature) }
                    ) continue

                    val method = when {
                        methodInfo.isConstructor -> methodInfo.loadClassAndGetConstructor()
                        else -> methodInfo.loadClassAndGetMethod()
                    }
                    val nullabilities = getMethodParameterNullabilities(methodInfo, method)

                    methodMetadataMap_[method] = MethodMetadata(methodInfo.minLineNum, nullabilities)
                }

                classMetadataMap_[classInfo.loadClass()] = ClassMetadata(classInfo.sourceFile)
            } catch (e: Throwable) {
                throw RuntimeException("An exception occurred while scanning class: ${classInfo.name}", e)
            }
        }

        scannedParams = true
    }

    private fun getMethodParameterNullabilities(methodInfo: MethodInfo, method: Executable): List<Boolean> {
        val nullabilities = methodInfo.parameterInfo.dropLast(if (method.isSuspend) 1 else 0).map { parameterInfo ->
            parameterInfo.annotationInfo.any { it.name.endsWith("Nullable") }
                    || parameterInfo.hasAnnotation(Optional::class.java)
        }

        return when {
            methodInfo.isStatic || methodInfo.isConstructor -> nullabilities
            //Pad with a non-null parameter to simulate the instance parameter
            else -> listOf(false) + nullabilities
        }
    }

    internal val Class<*>.sourceFile: String
        get() = (classMetadataMap[this]
            ?: throwUser("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")).sourceFile

    internal val KClass<*>.sourceFile: String
        get() = this.java.sourceFile

    internal val KParameter.isNullable: Boolean
        get() {
            val metadata = methodMetadataMap[function.javaMethodOrConstructor]
                ?: throwUser("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")
            val isNullableAnnotated =
                metadata.nullabilities[index]
            val isNullableMarked = type.isMarkedNullable

            return isNullableAnnotated || isNullableMarked
        }

    internal val KParameter.function: KFunction<*>
        get() {
            val callable = ReflectionMetadataAccessor.getParameterCallable(this)
            return callable as? KFunction<*>
                ?: throwInternal("Unable to get the function of a KParameter, callable is: $callable")
        }

    internal val KFunction<*>.declaringClass: KClass<*>
        get() = ReflectionMetadataAccessor.getFunctionDeclaringClass(this)

    internal val KFunction<*>.isJava
        get() = !declaringClass.hasAnnotation<Metadata>()

    internal val KFunction<*>.lineNumber: Int
        get() = (methodMetadataMap[this.javaMethodOrConstructor]
            ?: throwUser("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")).line

    private val Executable.isSuspend: Boolean
        get() = parameters.any { it.type == Continuation::class.java }
}