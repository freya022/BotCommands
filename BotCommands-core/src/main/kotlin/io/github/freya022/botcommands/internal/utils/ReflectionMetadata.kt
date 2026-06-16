package io.github.freya022.botcommands.internal.utils

import dev.freya02.bc.reflection.metadata.ReflectionMetadataScannerHelper
import io.github.classgraph.*
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.debugNull
import io.github.freya022.botcommands.api.core.reflect.annotations.ExperimentalReflectionApi
import io.github.freya022.botcommands.api.core.traceNull
import io.github.freya022.botcommands.api.core.utils.javaMethodOrConstructor
import io.github.freya022.botcommands.api.core.utils.mapToArray
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.core.utils.toImmutableMap
import io.github.freya022.botcommands.internal.core.ClassPathProcessor
import io.github.freya022.botcommands.internal.core.ClassPathProcessorProvider
import io.github.freya022.botcommands.internal.core.HandlersPresenceChecker
import io.github.freya022.botcommands.internal.core.service.BotCommandsBootstrap
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverSupertypeChecker
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.ClassMetadata
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.MethodMetadata
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Executable
import java.util.ServiceLoader
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.streams.asSequence

private typealias IsNullableAnnotated = Boolean

private val logger = KotlinLogging.logger { }

private interface LibClassesStrategy {
    fun configureClassGraph(classGraph: ClassGraph)

    fun partitionClasses(scanResult: ScanResult): Pair<List<ClassInfo>, List<ClassInfo>>

    fun filterLibClasses(libClasses: Collection<ClassInfo>): Collection<ClassInfo>
}

private class DefaultLibClassesStrategy(
    private val helper: ReflectionMetadataScannerHelper,
    private val bootstrap: BotCommandsBootstrap,
) : LibClassesStrategy {

    private val libPackages = ReflectionMetadataScanner::class.java.classLoader
        .resources("META-INF/bc.packages")
        .asSequence()
        .flatMap { it.readText().trim().lineSequence() }
        .toList()

    override fun configureClassGraph(classGraph: ClassGraph) {
        classGraph.acceptPackages(*libPackages.toTypedArray())
    }

    override fun partitionClasses(scanResult: ScanResult): Pair<List<ClassInfo>, List<ClassInfo>> {
        return scanResult.allClasses.partition(::isFromLib)
    }

    private fun isFromLib(classInfo: ClassInfo): Boolean {
        val pkgName = classInfo.packageName
        return libPackages.any { pkgName.startsWith(it) }
    }

    override fun filterLibClasses(libClasses: Collection<ClassInfo>): Collection<ClassInfo> {
        return ReflectionMetadataScannerHelper.filterClasses(
            helper.filterLibraryClasses(libClasses),
            onFileFacade = { ReflectionMetadataScanner.checkFacadeFactories(it, bootstrap) }
        )
    }
}

private class PreprocessedLibClassesStrategy : LibClassesStrategy {

    private val libClasses = ReflectionMetadataScanner::class.java.classLoader
        .resources("META-INF/bc.classes")
        .asSequence()
        .flatMap { it.readText().lineSequence() }
        .filter { it.isNotBlank() }
        .toHashSet()

    override fun configureClassGraph(classGraph: ClassGraph) {
        classGraph.acceptClasses(*libClasses.toTypedArray())
    }

    override fun partitionClasses(scanResult: ScanResult): Pair<List<ClassInfo>, List<ClassInfo>> {
        return scanResult.allClasses.partition { it.name in libClasses }
    }

    override fun filterLibClasses(libClasses: Collection<ClassInfo>): Collection<ClassInfo> {
        // Class list is already filtered
        return libClasses
    }
}

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
        internal val instanceOrNull: ReflectionMetadata? get() = _instance

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

    @OptIn(ExperimentalReflectionApi::class)
    private val classPathProcessors: List<ClassPathProcessor> = buildList {
        addAll(bootstrap.classPathProcessors)

        add(ResolverSupertypeChecker())
        add(HandlersPresenceChecker())

        ServiceLoader.load(ClassPathProcessorProvider::class.java).forEach {
            addAll(it.getProcessors(config))
        }
    }

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

        val helper = ReflectionMetadataScannerHelper(bootstrap::isService, bootstrap::isServiceFactory)
        val libClassesStrategy: LibClassesStrategy = if (config.usePreprocessedLibClassList) {
            PreprocessedLibClassesStrategy()
        } else {
            DefaultLibClassesStrategy(helper, bootstrap)
        }

        ClassGraph()
            .also(libClassesStrategy::configureClassGraph)
            .acceptPackages(*packages.toTypedArray())
            .acceptClasses(*classes.mapToArray { it.name })
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .disableModuleScanning()
            .scan()
            .use { scan ->
                val (libClasses, userClasses) = libClassesStrategy.partitionClasses(scan)
                libClasses
                    .let(libClassesStrategy::filterLibClasses)
                    .processClasses()

                userClasses
                    .let {
                        ReflectionMetadataScannerHelper.filterClasses(
                            it,
                            onFileFacade = { c -> checkFacadeFactories(c, bootstrap) },
                        )
                    }
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

                val postProcessData = ClassPathProcessor.PostProcessData(bootstrap.serviceContainer)
                classPathProcessors.forEach { it.postProcess(postProcessData) }
            }
    }

    private fun Collection<ClassInfo>.processClasses(): Unit = forEach { classInfo ->
        try {
            val clazz = tryGetClass(classInfo) ?: return@forEach
            val isService = bootstrap.isService(classInfo)
            val classData = ClassPathProcessor.ClassData(bootstrap.serviceContainer, classInfo, clazz, isService)

            processMethods(classData)

            classMetadataMap[clazz] = ClassMetadata(classInfo.sourceFile)

            classPathProcessors.forEach { it.processClass(classData) }
        } catch (e: Throwable) {
            e.rethrow("An exception occurred while scanning class: ${classInfo.name}")
        }
    }

    private fun tryGetClass(classInfo: ClassInfo): Class<*>? {
        // Ignore unknown classes
        return try {
            classInfo.loadClass()
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

    private fun processMethods(classData: ClassPathProcessor.ClassData) {
        for (methodInfo in classData.classInfo.declaredMethodAndConstructorInfo) {
            //Don't inspect methods with generics
            if (methodInfo.parameterInfo
                    .map { it.typeSignatureOrTypeDescriptor }
                    .any { it is TypeVariableSignature || (it is ArrayTypeSignature && it.elementTypeSignature is TypeVariableSignature) }
            ) continue

            val method: Executable = tryGetExecutable(methodInfo) ?: continue
            val nullabilities = getMethodParameterNullabilities(methodInfo, method)

            methodMetadataMap[method] = MethodMetadata(methodInfo.minLineNum, nullabilities)

            val isServiceFactory = bootstrap.isServiceFactory(methodInfo)
            val methodData = ClassPathProcessor.MethodData(classData, methodInfo, method, isServiceFactory)
            classPathProcessors.forEach { it.processMethod(methodData) }
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
                    logger.debugNull { "Ignoring method due to unsatisfied dependency in ${methodInfo.shortSignature} => ${e.message}" }
                }
            } else {
                throw e
            }
        }
    }

    private fun getMethodParameterNullabilities(methodInfo: MethodInfo, method: Executable): List<Boolean> {
        val nullabilities = methodInfo.parameterInfo.dropLast(if (method.isSuspend) 1 else 0).map { parameterInfo ->
            if (parameterInfo.annotationInfo.any { it.name.endsWith("Nullable") }) return@map true
            if (parameterInfo.typeSignatureOrTypeDescriptor.typeAnnotationInfo?.any { it.name.endsWith("Nullable") } == true) return@map true
            // TODO remove when annotation is removed
            if (parameterInfo.hasAnnotation("io.github.freya022.botcommands.api.commands.annotations.Optional")) return@map true

            false
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
        fun checkFacadeFactories(classInfo: ClassInfo, bootstrap: BotCommandsBootstrap) {
            classInfo.declaredMethodInfo.forEach { methodInfo ->
                check(!bootstrap.isServiceFactory(methodInfo)) {
                    "Top-level service factories are not supported: ${methodInfo.shortSignature}"
                }
            }
        }

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
    get() = ReflectionMetadata.instanceOrNull?.getClassMetadataOrNull(this)?.sourceFile

internal val KClass<*>.sourceFile: String
    get() = this.java.sourceFile

internal val KClass<*>.sourceFileOrNull: String?
    get() = this.java.sourceFileOrNull

val KParameter.isNullable: Boolean
    get() {
        val isNullableAnnotated = ReflectionMetadata.instance.getMethodMetadata(function).nullabilities[index]
        return isNullableAnnotated || type.isMarkedNullable
    }

internal val KFunction<*>.lineNumber: Int
    get() = ReflectionMetadata.instance.getMethodMetadata(this).line

internal val KFunction<*>.lineNumberOrNull: Int?
    get() = ReflectionMetadata.instanceOrNull?.getMethodMetadataOrNull(this)?.line
