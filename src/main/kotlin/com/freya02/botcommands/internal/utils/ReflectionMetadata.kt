package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.commands.annotations.Optional
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.service.ClassGraphProcessor
import com.freya02.botcommands.api.core.service.annotations.Condition
import com.freya02.botcommands.api.core.utils.javaMethodOrConstructor
import com.freya02.botcommands.api.core.utils.shortSignature
import com.freya02.botcommands.internal.commands.CommandsPresenceChecker
import com.freya02.botcommands.internal.core.BContextImpl
import com.freya02.botcommands.internal.core.HandlersPresenceChecker
import com.freya02.botcommands.internal.parameters.resolvers.ResolverSupertypeChecker
import com.freya02.botcommands.internal.utils.ReflectionUtils.function
import io.github.classgraph.*
import java.lang.reflect.Executable
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader
import kotlin.reflect.jvm.jvmName

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

    internal fun runScan(context: BContextImpl): List<KClass<*>> {
        val config = context.config
        val packages = config.packages
        //This is a requirement for ClassGraph to work correctly
        if (packages.isEmpty()) {
            throwUser("You must specify at least 1 package to scan classes from")
        }

        val scanned: List<Pair<ScanResult, ClassInfoList>> = buildList {
            ClassGraph()
                .acceptPackages("com.freya02.botcommands.api", "com.freya02.botcommands.internal")
                .enableMethodInfo()
                .enableAnnotationInfo()
                .disableModuleScanning()
                .disableNestedJarScanning()
                .scan()
                .also { scanResult -> // Don't keep test classes
                    add(scanResult to scanResult.allClasses.filter {
                        return@filter it.isServiceOrHasFactories(config)
                                || it.outerClasses.any { outer -> outer.isServiceOrHasFactories(config) }
                                || it.hasAnnotation(Condition::class.java)
                    })
                }

            ClassGraph()
                .acceptPackages(*config.packages.toTypedArray())
                .acceptClasses(*config.classes.map { it.name }.toTypedArray())
                .enableMethodInfo()
                .enableAnnotationInfo()
                .disableModuleScanning()
                .disableNestedJarScanning()
                .scan()
                .also { scanResult -> //No filtering is done as to allow checkers to log warnings/throw in case a service annotation is missing
                    add(scanResult to scanResult.allClasses)
                }
        }

        val lowercaseInnerClassRegex = Regex("\\$[a-z]")
        val classGraphProcessors = context.config.classGraphProcessors +
                listOf(context.serviceProviders, context.customConditionsContainer) +
                listOf(CommandsPresenceChecker(), ResolverSupertypeChecker(), HandlersPresenceChecker())
        return scanned.flatMap { (_, classes) ->
            classes
                .filter {
                    it.annotationInfo.directOnly()["kotlin.Metadata"]?.let { annotationInfo ->
                        //Only keep classes, not others such as file facades
                        val kind = KotlinClassHeader.Kind.getById(annotationInfo.parameterValues["k"].value as Int)
                        if (kind == KotlinClassHeader.Kind.FILE_FACADE) {
                            it.checkFacadeFactories(config)
                            return@filter false
                        } else if (kind != KotlinClassHeader.Kind.CLASS) {
                            return@filter false
                        }
                    }

                    if (lowercaseInnerClassRegex.containsMatchIn(it.name)) return@filter false
                    return@filter !it.isSynthetic && !it.isEnum && !it.isRecord
                }
                .processClasses(context, classGraphProcessors)
                .map { classInfo ->
                    val kClass = classInfo.loadClass().kotlin
                    //Fill map with all the @Command, @Resolver, etc... declarations
                    if (classInfo.isService(config)) {
                        classInfo.annotationInfo.forEach { annotationInfo ->
                            if (config.serviceConfig.serviceAnnotations.any { it.jvmName == annotationInfo.name }) {
                                context.serviceAnnotationsMap.put(
                                    annotationReceiver = kClass,
                                    annotationType = annotationInfo.classInfo.loadClass(Annotation::class.java).kotlin,
                                    annotation = annotationInfo.loadClassAndInstantiate()
                                )
                            }
                        }
                    }

                    kClass
                }
        }.also {
            classGraphProcessors.forEach { it.postProcess(context) }
            scanned.forEach { (scanResult, _) -> scanResult.close() }

            scannedParams = true
        }
    }

    private fun ClassInfo.checkFacadeFactories(config: BConfig) {
        this.declaredMethodInfo.forEach { methodInfo ->
            check(!methodInfo.isService(config)) {
                "Top-level service factories are not supported: ${methodInfo.shortSignature}"
            }
        }
    }

    internal fun ClassInfo.isService(config: BConfig) =
        config.serviceConfig.serviceAnnotations.any { serviceAnnotation -> hasAnnotation(serviceAnnotation.jvmName) }

    internal fun MethodInfo.isService(config: BConfig) =
        config.serviceConfig.serviceAnnotations.any { serviceAnnotation -> hasAnnotation(serviceAnnotation.jvmName) }

    private fun ClassInfo.isServiceOrHasFactories(config: BConfig) =
        isService(config) || methodInfo.any { it.isService(config) }

    private fun List<ClassInfo>.processClasses(context: BContextImpl, classGraphProcessors: List<ClassGraphProcessor>): List<ClassInfo> {
        return onEach { classInfo ->
            try {
                val kClass = classInfo.loadClass().kotlin

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

                    classGraphProcessors.forEach { it.processMethod(context, methodInfo, method, classInfo, kClass) }
                }

                classMetadataMap_[classInfo.loadClass()] = ClassMetadata(classInfo.sourceFile)

                classGraphProcessors.forEach { it.processClass(context, classInfo, kClass) }
            } catch (e: Throwable) {
                throw RuntimeException("An exception occurred while scanning class: ${classInfo.name}", e)
            }
        }
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

    internal val KFunction<*>.lineNumber: Int
        get() = (methodMetadataMap[this.javaMethodOrConstructor]
            ?: throwUser("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")).line

    private val Executable.isSuspend: Boolean
        get() = parameters.any { it.type == Continuation::class.java }
}