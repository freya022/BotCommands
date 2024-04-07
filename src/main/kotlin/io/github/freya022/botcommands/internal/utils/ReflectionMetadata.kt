package io.github.freya022.botcommands.internal.utils

import io.github.classgraph.*
import io.github.freya022.botcommands.api.commands.annotations.Optional
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.utils.javaMethodOrConstructor
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.CommandsPresenceChecker
import io.github.freya022.botcommands.internal.core.HandlersPresenceChecker
import io.github.freya022.botcommands.internal.core.service.ServiceBootstrap
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverSupertypeChecker
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val logger = KotlinLogging.logger { }

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

    internal fun runScan(config: BConfig, serviceBootstrap: ServiceBootstrap) {
        val packages = config.packages
        //This is a requirement for ClassGraph to work correctly
        if (packages.isEmpty() && config.classes.isEmpty()) {
            throwUser("You must specify at least 1 package or class to scan from")
        }

        logger.debug { "Scanning packages: ${config.packages.joinToString()}" }
        if (config.classes.isNotEmpty()) {
            logger.debug { "Scanning additional classes: ${config.classes.joinToString { it.simpleNestedName }}" }
        }

        val scanned: List<Pair<ScanResult, ClassInfoList>> = buildList {
            ClassGraph()
                .acceptPackages("io.github.freya022.botcommands.api", "io.github.freya022.botcommands.internal")
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
        val classGraphProcessors = config.classGraphProcessors +
                serviceBootstrap.classGraphProcessors +
                listOf(CommandsPresenceChecker(), ResolverSupertypeChecker(), HandlersPresenceChecker())
        //TODO refactor this so we don't need to do two scans using spring

        // Deduplicate classes from the two scans
        // They are duplicated due to how we detect spring-scanned classes
        scanned.flatMapTo(hashSetOf()) { (_, classes) -> classes }
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
                .processClasses(config, classGraphProcessors)
                .also {
                    classGraphProcessors.forEach { it.postProcess() }
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

    private fun ClassInfo.isService(config: BConfig): Boolean {
        val declaredAnnotations = annotations.directOnly()
        return config.serviceConfig.serviceAnnotations.any { serviceAnnotation -> declaredAnnotations.containsName(serviceAnnotation.jvmName) }
    }

    private fun MethodInfo.isService(config: BConfig): Boolean {
        val declaredAnnotations = annotationInfo.directOnly()
        return config.serviceConfig.serviceAnnotations.any { serviceAnnotation -> declaredAnnotations.containsName(serviceAnnotation.jvmName) }
    }

    private fun ClassInfo.isServiceOrHasFactories(config: BConfig) =
        isService(config) || methodInfo.any { it.isService(config) }

    private fun List<ClassInfo>.processClasses(config: BConfig, classGraphProcessors: List<ClassGraphProcessor>): List<ClassInfo> {
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

                    val isServiceFactory = methodInfo.isService(config)
                    classGraphProcessors.forEach { it.processMethod(methodInfo, method, classInfo, kClass, isServiceFactory) }
                }

                classMetadataMap_[classInfo.loadClass()] = ClassMetadata(classInfo.sourceFile)

                val isService = classInfo.isService(config)
                classGraphProcessors.forEach { it.processClass(classInfo, kClass, isService) }
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

            return isNullableAnnotated || type.isMarkedNullable
        }

    internal val KFunction<*>.lineNumber: Int
        get() = (methodMetadataMap[this.javaMethodOrConstructor]
            ?: throwUser("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")).line

    private val Executable.isSuspend: Boolean
        get() = parameters.any { it.type == Continuation::class.java }
}