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
        val classes = config.classes
        require(packages.isNotEmpty() || classes.isNotEmpty()) {
            throwUser("You must specify at least 1 package or class to scan from")
        }

        if (packages.isNotEmpty())
            logger.debug { "Scanning packages: ${packages.joinToString()}" }
        if (classes.isNotEmpty())
            logger.debug { "Scanning classes: ${classes.joinToString { it.simpleNestedName }}" }

        val classGraphProcessors = config.classGraphProcessors +
                serviceBootstrap.classGraphProcessors +
                listOf(CommandsPresenceChecker(), ResolverSupertypeChecker(), HandlersPresenceChecker())

        ClassGraph()
            .acceptPackages(
                "io.github.freya022.botcommands.api",
                "io.github.freya022.botcommands.internal",
                *packages.toTypedArray()
            )
            .acceptClasses(*classes.map { it.name }.toTypedArray())
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .disableModuleScanning()
            .disableNestedJarScanning()
            .scan()
            .use { scan ->
                scan.allClasses
                    .filterLibraryClasses(config)
                    .filterClasses(config)
                    .also { classes ->
                        val userClasses = classes.filterNot { it.isFromLib() }
                        if (logger.isTraceEnabled()) {
                            logger.trace { "Found ${userClasses.size} user classes: ${userClasses.joinToString { it.simpleNestedName }}" }
                        } else {
                            logger.debug { "Found ${userClasses.size} user classes" }
                        }
                    }
                    .processClasses(config, classGraphProcessors)

                classGraphProcessors.forEach(ClassGraphProcessor::postProcess)
            }

        scannedParams = true
    }

    private fun List<ClassInfo>.filterLibraryClasses(config: BConfig): List<ClassInfo> = filter {
        // Only keep api/internal classes which are services, or contain service factories,
        // or their outer class contains one,
        // or they have a Condition
        if (it.isFromLib())
            return@filter it.isServiceOrHasFactories(config)
                    || it.outerClasses.any { outer -> outer.isServiceOrHasFactories(config) }
                    || it.hasAnnotation(Condition::class.java)
        return@filter true
    }

    private fun ClassInfo.isFromLib() =
        packageName.startsWith("io.github.freya022.botcommands.api") || packageName.startsWith("io.github.freya022.botcommands.internal")

    private val lowercaseInnerClassRegex = Regex("\\$[a-z]")
    private fun List<ClassInfo>.filterClasses(config: BConfig): List<ClassInfo> = filter {
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
                // Ignore unknown classes
                val kClass = runCatching {
                    classInfo.loadClass().kotlin
                }.onFailure {
                    // ClassGraph wraps Class#forName exceptions in an IAE
                    if (it is IllegalArgumentException) {
                        val cause = it.cause
                        if (cause is ClassNotFoundException || cause is NoClassDefFoundError) {
                            logger.debug { "Ignoring ${classInfo.name} due to unsatisfied dependency ${cause.message}" }
                            return@onEach
                        }
                    }
                }.getOrThrow()

                for (methodInfo in classInfo.declaredMethodAndConstructorInfo) {
                    //Don't inspect methods with generics
                    if (methodInfo.parameterInfo
                            .map { it.typeSignatureOrTypeDescriptor }
                            .any { it is TypeVariableSignature || (it is ArrayTypeSignature && it.elementTypeSignature is TypeVariableSignature) }
                    ) continue

                    // Ignore methods with missing dependencies (such as parameters from unknown dependencies)
                    val method: Executable = try {
                        when {
                            methodInfo.isConstructor -> methodInfo.loadClassAndGetConstructor()
                            else -> methodInfo.loadClassAndGetMethod()
                        }
                    } catch (e: NoClassDefFoundError) {
                        if (logger.isTraceEnabled())
                            logger.trace(e) { "Ignoring method due to unsatisfied dependencies in ${methodInfo.shortSignature}" }
                        else
                            logger.debug { "Ignoring method due to unsatisfied dependency ${e.message} in ${methodInfo.shortSignature}" }
                        continue
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