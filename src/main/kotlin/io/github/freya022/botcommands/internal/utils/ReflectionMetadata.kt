package io.github.freya022.botcommands.internal.utils

import io.github.classgraph.*
import io.github.freya022.botcommands.api.commands.annotations.Optional
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.debugNull
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.traceNull
import io.github.freya022.botcommands.api.core.utils.containsAny
import io.github.freya022.botcommands.api.core.utils.javaMethodOrConstructor
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.CommandsPresenceChecker
import io.github.freya022.botcommands.internal.core.HandlersPresenceChecker
import io.github.freya022.botcommands.internal.core.service.BotCommandsBootstrap
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

    internal fun runScan(config: BConfig, bootstrap: BotCommandsBootstrap) {
        val packages = config.packages
        val classes = config.classes
        require(packages.isNotEmpty() || classes.isNotEmpty()) {
            throwArgument("You must specify at least 1 package or class to scan from")
        }

        if (packages.isNotEmpty())
            logger.debug { "Scanning packages: ${packages.joinToString()}" }
        if (classes.isNotEmpty())
            logger.debug { "Scanning classes: ${classes.joinToString { it.simpleNestedName }}" }

        val classGraphProcessors = config.classGraphProcessors +
                bootstrap.classGraphProcessors +
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
                val (libClasses, userClasses) = scan.allClasses.partition { it.isFromLib() }
                libClasses
                    .filterLibraryClasses(bootstrap)
                    .filterClasses(bootstrap)
                    .processClasses(bootstrap, classGraphProcessors)

                userClasses
                    .filterClasses(bootstrap)
                    .also {
                        if (userClasses.isEmpty()) {
                            logger.warn { "Found no user classes to scan, check the packages set in ${BConfigBuilder::packages.reference}" }
                        } else if (logger.isTraceEnabled()) {
                            logger.trace { "Found ${userClasses.size} user classes: ${userClasses.joinToString { it.simpleNestedName }}" }
                        } else {
                            logger.debug { "Found ${userClasses.size} user classes" }
                        }
                    }
                    .processClasses(bootstrap, classGraphProcessors)

                classGraphProcessors.forEach(ClassGraphProcessor::postProcess)
            }

        scannedParams = true
    }

    private fun List<ClassInfo>.filterLibraryClasses(bootstrap: BotCommandsBootstrap): List<ClassInfo> {
        // Get types referenced by factories so we get metadata from those as well
        val referencedTypes = asSequence()
            .flatMap { it.methodInfo }
            .filter { bootstrap.isService(it) }
            .mapTo(hashSetOf()) { it.typeDescriptor.resultType.toString() }

        fun ClassInfo.isServiceOrHasFactories(): Boolean {
            return bootstrap.isService(this) || methodInfo.any { bootstrap.isService(it) }
        }

        return filter { classInfo ->
            if (classInfo.isServiceOrHasFactories()) return@filter true

            // Get metadata from all classes that extend a referenced type
            // As we can't know exactly what object a factory could return
            val superclasses = (classInfo.superclasses + classInfo.interfaces + classInfo).mapTo(hashSetOf()) { it.name }
            if (superclasses.containsAny(referencedTypes)) return@filter true

            if (classInfo.outerClasses.any { it.isServiceOrHasFactories() }) return@filter true
            if (classInfo.hasAnnotation(Condition::class.java)) return@filter true
            if (classInfo.interfaces.containsAny(CustomConditionChecker::class.java, ConditionalServiceChecker::class.java)) return@filter true

            return@filter false
        }
    }

    private fun ClassInfo.isFromLib() =
        packageName.startsWith("io.github.freya022.botcommands.api") || packageName.startsWith("io.github.freya022.botcommands.internal")

    private fun ClassInfoList.containsAny(vararg classes: Class<*>): Boolean = classes.any { containsName(it.name) }

    private val lowercaseInnerClassRegex = Regex("\\$[a-z]")
    private fun List<ClassInfo>.filterClasses(bootstrap: BotCommandsBootstrap): List<ClassInfo> = filter {
        it.annotationInfo.directOnly()["kotlin.Metadata"]?.let { annotationInfo ->
            //Only keep classes, not others such as file facades
            val kind = KotlinClassHeader.Kind.getById(annotationInfo.parameterValues["k"].value as Int)
            if (kind == KotlinClassHeader.Kind.FILE_FACADE) {
                it.checkFacadeFactories(bootstrap)
                return@filter false
            } else if (kind != KotlinClassHeader.Kind.CLASS) {
                return@filter false
            }
        }

        if (lowercaseInnerClassRegex.containsMatchIn(it.name)) return@filter false
        return@filter !it.isSynthetic && !it.isEnum && !it.isRecord
    }

    private fun ClassInfo.checkFacadeFactories(bootstrap: BotCommandsBootstrap) {
        this.declaredMethodInfo.forEach { methodInfo ->
            check(!bootstrap.isService(methodInfo)) {
                "Top-level service factories are not supported: ${methodInfo.shortSignature}"
            }
        }
    }

    private fun List<ClassInfo>.processClasses(bootstrap: BotCommandsBootstrap, classGraphProcessors: List<ClassGraphProcessor>): List<ClassInfo> {
        return onEach { classInfo ->
            try {
                val kClass = tryGetClass(classInfo) ?: return@onEach

                processMethods(bootstrap, classGraphProcessors, classInfo, kClass)

                classMetadataMap_[kClass.java] = ClassMetadata(classInfo.sourceFile)

                val isDefaultService = bootstrap.isService(classInfo)
                classGraphProcessors.forEach { it.processClass(classInfo, kClass, isDefaultService) }
            } catch (e: Throwable) {
                e.rethrow("An exception occurred while scanning class: ${classInfo.name}")
            }
        }
    }

    private fun tryGetClass(classInfo: ClassInfo): KClass<*>? {
        // Ignore unknown classes
        return try {
            classInfo.loadClass().kotlin
        } catch(e: IllegalArgumentException) {
            // ClassGraph wraps Class#forName exceptions in an IAE
            val cause = e.cause
            if (cause is ClassNotFoundException || cause is NoClassDefFoundError) {
                return if (logger.isTraceEnabled()) {
                    logger.traceNull(e) { "Ignoring ${classInfo.name} due to unsatisfied dependency" }
                } else {
                    logger.debugNull { "Ignoring ${classInfo.name} due to unsatisfied dependency: ${cause.message}" }
                }
            } else {
                throw e
            }
        }
    }

    private fun processMethods(
        bootstrap: BotCommandsBootstrap,
        classGraphProcessors: List<ClassGraphProcessor>,
        classInfo: ClassInfo,
        kClass: KClass<out Any>,
    ) {
        for (methodInfo in classInfo.declaredMethodAndConstructorInfo) {
            //Don't inspect methods with generics
            if (methodInfo.parameterInfo
                    .map { it.typeSignatureOrTypeDescriptor }
                    .any { it is TypeVariableSignature || (it is ArrayTypeSignature && it.elementTypeSignature is TypeVariableSignature) }
            ) continue

            val method: Executable = tryGetExecutable(methodInfo) ?: continue
            val nullabilities = getMethodParameterNullabilities(methodInfo, method)

            methodMetadataMap_[method] = MethodMetadata(methodInfo.minLineNum, nullabilities)

            val isServiceFactory = bootstrap.isService(methodInfo)
            classGraphProcessors.forEach { it.processMethod(methodInfo, method, classInfo, kClass, isServiceFactory) }
        }
    }

    private fun tryGetExecutable(methodInfo: MethodInfo): Executable? {
        fun handleException(e: Throwable): Executable? {
            return if (logger.isTraceEnabled()) {
                logger.traceNull(e) { "Ignoring method due to unsatisfied dependencies in ${methodInfo.shortSignature}" }
            } else {
                logger.debugNull { "Ignoring method due to unsatisfied dependency ${e.message} in ${methodInfo.shortSignature}" }
            }
        }

        // Ignore methods with missing dependencies (such as parameters from unknown dependencies)
        try {
            return when {
                methodInfo.isConstructor -> methodInfo.loadClassAndGetConstructor()
                else -> methodInfo.loadClassAndGetMethod()
            }
        } catch (e: NoClassDefFoundError) { // In case the return type or a parameter is unknown
            return handleException(e) //TODO inline back in IAE handler when CG rethrows those as IAE
        } catch(e: IllegalArgumentException) {
            // ClassGraph wraps exceptions in an IAE
            val cause = e.cause
            if (cause is ClassNotFoundException || cause is NoClassDefFoundError) {
                return handleException(cause)
            } else {
                throw e
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
            ?: throwArgument("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")).sourceFile

    internal val Class<*>.sourceFileOrNull: String?
        get() = classMetadataMap[this]?.sourceFile

    internal val KClass<*>.sourceFile: String
        get() = this.java.sourceFile

    internal val KClass<*>.sourceFileOrNull: String?
        get() = this.java.sourceFileOrNull

    internal val KParameter.isNullable: Boolean
        get() {
            val metadata = methodMetadataMap[function.javaMethodOrConstructor]
                ?: throwArgument("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")
            val isNullableAnnotated =
                metadata.nullabilities[index]

            return isNullableAnnotated || type.isMarkedNullable
        }

    internal val KFunction<*>.lineNumber: Int
        get() = lineNumberOrNull
            ?: throwArgument("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")

    internal val KFunction<*>.lineNumberOrNull: Int?
        get() = methodMetadataMap[this.javaMethodOrConstructor]?.line

    private val Executable.isSuspend: Boolean
        get() = parameters.any { it.type == Continuation::class.java }
}