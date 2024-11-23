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
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.internal.commands.CommandsPresenceChecker
import io.github.freya022.botcommands.internal.core.HandlersPresenceChecker
import io.github.freya022.botcommands.internal.core.service.BotCommandsBootstrap
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverSupertypeChecker
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.ClassMetadata
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.MethodMetadata
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Executable
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader

private typealias IsNullableAnnotated = Boolean

private val logger = KotlinLogging.logger { }

internal class ReflectionMetadata(
    private val classMetadataMap: Map<Class<*>, ClassMetadata>,
    private val methodMetadataMap: Map<Executable, MethodMetadata>,
) {

    internal class ClassMetadata(val sourceFile: String)
    internal class MethodMetadata(val line: Int, val nullabilities: List<IsNullableAnnotated>)

    internal fun getClassMetadata(clazz: Class<*>): ClassMetadata {
        return classMetadataMap[clazz]
            ?: throwArgument("Tried to access a Class which hasn't been scanned: $this, the class must be accessible and in the search path")
    }

    internal fun getClassMetadataOrNull(clazz: Class<*>): ClassMetadata? {
        return classMetadataMap[clazz]
    }

    internal fun getMethodMetadata(executable: Executable): MethodMetadata {
        return methodMetadataMap[executable]
            ?: throwArgument("Tried to access a Method which hasn't been scanned: $this, the method must be accessible and in the search path")
    }

    internal fun getMethodMetadataOrNull(executable: Executable): MethodMetadata? {
        return methodMetadataMap[executable]
    }

    internal companion object {

        private var _instance: ReflectionMetadata? = null
        internal val instance: ReflectionMetadata
            get() = _instance ?: throwInternal("Tried to access reflection metadata but they haven't been scanned yet")

        internal fun runScan(config: BConfig, bootstrap: BotCommandsBootstrap) {
            _instance = ReflectionMetadataScanner.scan(config, bootstrap)
        }
    }
}

private fun ReflectionMetadata.getMethodMetadata(function: KFunction<*>): MethodMetadata {
    return getMethodMetadata(function.javaMethodOrConstructor)
}

private fun ReflectionMetadata.getMethodMetadataOrNull(function: KFunction<*>): MethodMetadata? {
    return getMethodMetadataOrNull(function.javaMethodOrConstructor)
}

private class ReflectionMetadataScanner private constructor(
    private val config: BConfig,
    private val bootstrap: BotCommandsBootstrap
) {

    private val classGraphProcessors: List<ClassGraphProcessor> =
        config.classGraphProcessors +
                bootstrap.classGraphProcessors +
                listOf(CommandsPresenceChecker(), ResolverSupertypeChecker(), HandlersPresenceChecker())

    private val classMetadataMap: MutableMap<Class<*>, ClassMetadata> = hashMapOf()
    private val methodMetadataMap: MutableMap<Executable, MethodMetadata> = hashMapOf()

    private fun scan() {
        val packages = config.packages
        val classes = config.classes
        require(packages.isNotEmpty() || classes.isNotEmpty()) {
            "You must specify at least 1 package or class to scan from"
        }

        if (packages.isNotEmpty())
            logger.debug { "Scanning packages: ${packages.joinToString()}" }
        if (classes.isNotEmpty())
            logger.debug { "Scanning classes: ${classes.joinToString { it.simpleNestedName }}" }

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
            .scan()
            .use { scan ->
                val (libClasses, userClasses) = scan.allClasses.partition { it.isFromLib() }
                libClasses
                    .filterLibraryClasses()
                    .filterClasses()
                    .processClasses()

                userClasses
                    .filterClasses()
                    .also {
                        if (userClasses.isEmpty()) {
                            logger.warn { "Found no user classes to scan, check the packages set in ${BConfigBuilder::packages.reference}" }
                        } else if (logger.isTraceEnabled()) {
                            logger.trace { "Found ${userClasses.size} user classes: ${userClasses.joinToString { it.simpleNestedName }}" }
                        } else {
                            logger.debug { "Found ${userClasses.size} user classes" }
                        }
                    }
                    .processClasses()

                classGraphProcessors.forEach(ClassGraphProcessor::postProcess)
            }
    }

    private fun ClassInfo.isFromLib() =
        packageName.startsWith("io.github.freya022.botcommands.api") || packageName.startsWith("io.github.freya022.botcommands.internal")

    private fun List<ClassInfo>.filterLibraryClasses(): List<ClassInfo> {
        // Get types referenced by factories so we get metadata from those as well
        val referencedTypes = asSequence()
            .flatMap { it.methodInfo }
            .filter { bootstrap.isServiceFactory(it) }
            .mapTo(hashSetOf()) { it.typeDescriptor.resultType.toString() }

        fun ClassInfo.isServiceOrHasFactories(): Boolean {
            return bootstrap.isService(this) || methodInfo.any { bootstrap.isServiceFactory(it) }
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

    private fun ClassInfoList.containsAny(vararg classes: Class<*>): Boolean = classes.any { containsName(it.name) }

    private val lowercaseInnerClassRegex = Regex("\\$[a-z]")
    private fun List<ClassInfo>.filterClasses(): List<ClassInfo> = filter {
        it.annotationInfo.directOnly()["kotlin.Metadata"]?.let { annotationInfo ->
            //Only keep classes, not others such as file facades
            val kind = KotlinClassHeader.Kind.getById(annotationInfo.parameterValues["k"].value as Int)
            if (kind == KotlinClassHeader.Kind.FILE_FACADE) {
                it.checkFacadeFactories()
                return@filter false
            } else if (kind != KotlinClassHeader.Kind.CLASS) {
                return@filter false
            }
        }

        if (lowercaseInnerClassRegex.containsMatchIn(it.name)) return@filter false
        return@filter !it.isSynthetic && !it.isEnum && !it.isRecord
    }

    private fun ClassInfo.checkFacadeFactories() {
        this.declaredMethodInfo.forEach { methodInfo ->
            check(!bootstrap.isServiceFactory(methodInfo)) {
                "Top-level service factories are not supported: ${methodInfo.shortSignature}"
            }
        }
    }

    private fun List<ClassInfo>.processClasses(): List<ClassInfo> {
        return onEach { classInfo ->
            try {
                val kClass = tryGetClass(classInfo) ?: return@onEach

                processMethods(classInfo, kClass)

                classMetadataMap[kClass.java] = ClassMetadata(classInfo.sourceFile)

                val isService = bootstrap.isService(classInfo)
                classGraphProcessors.forEach { it.processClass(classInfo, kClass, isService) }
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

            methodMetadataMap[method] = MethodMetadata(methodInfo.minLineNum, nullabilities)

            val isServiceFactory = bootstrap.isServiceFactory(methodInfo)
            classGraphProcessors.forEach { it.processMethod(methodInfo, method, classInfo, kClass, isServiceFactory) }
        }
    }

    private fun tryGetExecutable(methodInfo: MethodInfo): Executable? {
        // Ignore methods with missing dependencies (such as parameters from unknown dependencies)
        try {
            return when {
                methodInfo.isConstructor -> methodInfo.loadClassAndGetConstructor()
                else -> methodInfo.loadClassAndGetMethod()
            }
        } catch(e: IllegalArgumentException) {
            // ClassGraph wraps exceptions in an IAE
            val cause = e.cause
            if (cause is ClassNotFoundException || cause is NoClassDefFoundError) {
                return if (logger.isTraceEnabled()) {
                    logger.traceNull(e) { "Ignoring method due to unsatisfied dependencies in ${methodInfo.shortSignature}" }
                } else {
                    logger.debugNull { "Ignoring method due to unsatisfied dependency ${e.message} in ${methodInfo.shortSignature}" }
                }
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

    private val Executable.isSuspend: Boolean
        get() = parameters.any { it.type == Continuation::class.java }

    companion object {
        fun scan(
            config: BConfig,
            bootstrap: BotCommandsBootstrap,
        ): ReflectionMetadata {
            val scanner = ReflectionMetadataScanner(config, bootstrap)
            scanner.scan()
            return ReflectionMetadata(
                scanner.classMetadataMap.toImmutableMap(),
                scanner.methodMetadataMap.toImmutableMap(),
            )
        }
    }
}

internal val Class<*>.sourceFile: String
    get() = ReflectionMetadata.instance.getClassMetadata(this).sourceFile

internal val Class<*>.sourceFileOrNull: String?
    get() = ReflectionMetadata.instance.getClassMetadataOrNull(this)?.sourceFile

internal val KClass<*>.sourceFile: String
    get() = this.java.sourceFile

internal val KClass<*>.sourceFileOrNull: String?
    get() = this.java.sourceFileOrNull

internal val KParameter.isNullable: Boolean
    get() {
        val isNullableAnnotated = ReflectionMetadata.instance.getMethodMetadata(function).nullabilities[index]
        return isNullableAnnotated || type.isMarkedNullable
    }

internal val KFunction<*>.lineNumber: Int
    get() = ReflectionMetadata.instance.getMethodMetadata(this).line

internal val KFunction<*>.lineNumberOrNull: Int?
    get() = ReflectionMetadata.instance.getMethodMetadataOrNull(this)?.line