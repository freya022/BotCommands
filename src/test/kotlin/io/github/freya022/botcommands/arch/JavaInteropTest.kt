package io.github.freya022.botcommands.arch

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withPublicOrDefaultModifier
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.ext.list.withoutAnnotationNamed
import com.lemonappdev.konsist.api.ext.provider.hasAnnotationOf
import com.lemonappdev.konsist.api.verify.assertTrue
import io.github.classgraph.BaseTypeSignature
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.time.Duration as JavaDuration
import java.util.concurrent.TimeUnit
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction
import kotlin.test.Test
import kotlin.test.fail
import kotlin.time.Duration

private val ktReflectNameRegex = Regex("kotlin\\.reflect\\.[\\w.]+")

class JavaInteropTest {

    @Test
    fun `Check all functions accepting kotlin-reflect instances have Java equivalents`() {
        val failedFunctions = arrayListOf<String>()

        ClassGraph()
            .acceptPackages("*.botcommands*.api.*")
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use { scan ->
                scan.allClasses
                    .asSequence()
                    // Only real class methods
                    .filter { it.isKotlinExplicitClass() }
                    .flatMap { it.declaredMethodInfo }
                    .filterNot { it.hasAnnotation("io.github.freya022.botcommands.internal.core.annotations.SkipJavaReflectionOverload") }
                    .filterNot { it.hasAnnotation("kotlin.Deprecated") }
                    .filterNot { it.isSynthetic }
                    .filter { "kotlin/reflect" in it.typeSignatureOrTypeDescriptorStr.substringBefore(')') /* only look parameters */ }
                    .filterNot { it.isOverriding() }
                    .forEach { method ->
                        // Try to find Java reflection equivalent
                        val parameterTypes = method.getParameterTypesString()
                        val expectedParameterTypes = parameterTypes.replace(ktReflectNameRegex) { getJavaReflectName(it.value) }

                        val hasJavaEquivalent = method.classInfo
                            .declaredMethodInfo
                            .filter { it !== method && it.name == method.name }
                            .any { expectedParameterTypes == it.getParameterTypesString() }

                        if (!hasJavaEquivalent) {
                            val parameters = method.parameterInfo.joinToString { it.typeDescriptor.toStringWithSimpleNames() }
                            failedFunctions += "${method.classInfo.shortQualifiedName} : ${method.name}($parameters)"
                        }
                    }
            }

        if (failedFunctions.isNotEmpty()) {
            fail("Some functions do not comply:\n${failedFunctions.joinToString("\n")}")
        }
    }

    private fun ClassInfo.isKotlinExplicitClass(): Boolean {
        val metadataInfo = this.annotationInfo.directOnly().get(Metadata::class.java.name)
            ?: return false
        val metadata = metadataInfo.loadClassAndInstantiate() as Metadata
        return when (KotlinClassMetadata.readStrict(metadata)) {
            is KotlinClassMetadata.Class -> true
            else -> false
        }
    }

    private fun MethodInfo.isOverriding(): Boolean {
        val expectedDescriptor = this.typeDescriptorStr
        return (classInfo.superclasses + classInfo.interfaces).any { superType ->
            superType.declaredMethodInfo.any { superMethod ->
                superMethod.name == this.name && superMethod.typeDescriptorStr == expectedDescriptor
            }
        }
    }

    private fun getJavaReflectName(kotlinReflectName: String): String {
        return when (kotlinReflectName) {
            KClass::class.java.name -> Class::class.java.name
            KFunction::class.java.name -> Method::class.java.name
            KType::class.java.name -> Type::class.java.name
            else -> error("Unmapped kotlin-reflect type: $kotlinReflectName")
        }
    }

    private fun MethodInfo.getParameterTypesString(): String {
        return parameterInfo.joinToString { it.typeSignatureOrTypeDescriptor.toString() }
    }

    @Test
    fun `Check all functions accepting Kotlin's Duration have overloads with Java's Duration`() {
        val failedFunctions = arrayListOf<String>()

        forEachClass({
            acceptPackages("*.botcommands*.api.*")
            enableMethodInfo()
            enableAnnotationInfo()
        }) { classInfo ->
            // Ignore if outside a class
            if (!classInfo.isKotlinExplicitClass()) return@forEachClass

            val declaredMemberFunctions by lazy {
                classInfo.loadClass().kotlin.declaredMemberFunctions
            }

            // Small optimization to avoid loading reflection data of every class
            classInfo.declaredMethodInfo.forEach { methodInfo ->
                if (methodInfo.isSynthetic) return@forEach

                // Attempt to look for kotlin.time.Duration only if there is a `long` parameter,
                // as this is what it compiles to
                val hasLongParam = methodInfo.parameterInfo.any { (it.typeDescriptor as? BaseTypeSignature)?.typeSignatureChar == 'J' }
                if (!hasLongParam) return@forEach

                // Make sure we have a k.t.Duration
                val function = methodInfo.loadClassAndGetMethod().kotlinFunction ?: return@forEach
                if (function.parameters.none { it.type.jvmErasureOrNull == Duration::class }) return@forEach

                context(failedFunctions, declaredMemberFunctions) {
                    checkJavaDurationOverload(methodInfo, function)
                    checkRawTimeUnitOverload(methodInfo, function)
                }
            }
        }

        if (failedFunctions.isNotEmpty()) {
            fail("Some functions do not comply:\n${failedFunctions.joinToString("\n")}")
        }
    }

    private fun forEachClass(configure: ClassGraph.() -> Unit, block: (classInfo: ClassInfo) -> Unit) {
        ClassGraph()
            .apply(configure)
            .scan()
            .use { scan ->
                scan.allClasses.forEach(block)
            }
    }

    context(failedFunctions: MutableCollection<String>, declaredMemberFunctions: Collection<KFunction<*>>)
    private fun checkJavaDurationOverload(methodInfo: MethodInfo, function: KFunction<*>) {
        checkOverload(
            desc = "Missing j.t.Duration",
            methodInfo = methodInfo,
            function = function,
            durationReplacements = listOf(JavaDuration::class.java)
        )
    }

    context(failedFunctions: MutableCollection<String>, declaredMemberFunctions: Collection<KFunction<*>>)
    private fun checkRawTimeUnitOverload(methodInfo: MethodInfo, function: KFunction<*>) {
        checkOverload(
            desc = "Missing raw time + TimeUnit",
            methodInfo = methodInfo,
            function = function,
            durationReplacements = listOf(Long::class.javaPrimitiveType!!, TimeUnit::class.java)
        )
    }

    context(failedFunctions: MutableCollection<String>, declaredMemberFunctions: Collection<KFunction<*>>)
    private fun checkOverload(desc: String, methodInfo: MethodInfo, function: KFunction<*>, durationReplacements: List<Class<*>>) {
        val expectedOverloadTypes: List<Class<*>?> = buildList {
            function.parameters.map { it.type.jvmErasureOrNull?.java }.forEach { originalType ->
                if (originalType == Duration::class.java) {
                    addAll(durationReplacements)
                } else {
                    add(originalType)
                }
            }
        }

        val hasExpectedOverload = declaredMemberFunctions.any { function ->
            function.parameters.map { it.type.jvmErasureOrNull?.java } == expectedOverloadTypes
        }

        if (!hasExpectedOverload) {
            val parameters = function.valueParameters.joinToString { it.type.jvmErasureOrNull?.simpleName ?: "<function type>" }
            failedFunctions += "$desc : ${methodInfo.classInfo.shortQualifiedName} : ${function.name}($parameters)"
        }
    }

    private val KType.jvmErasureOrNull: KClass<*>?
        get() {
            if (classifier == null) return null
            return jvmErasure
        }

    @Test
    fun `Check all object functions have @JvmStatic`() {
        Konsist.scopeFromProduction()
            .objects(includeNested = true)
            .filter { it.packagee!!.name.contains("api") }
            .functions(includeNested = false, includeLocal = false)
            .withPublicOrDefaultModifier()
            .withoutAnnotationNamed(JvmSynthetic::class.java.simpleName)
            .assertTrue(strict = true) { it.hasAnnotationOf<JvmStatic>() }
    }

    @Test
    fun `Check all object properties have @JvmStatic`() {
        Konsist.scopeFromProduction()
            .objects(includeNested = true)
            .filter { it.packagee!!.name.contains("api") }
            .properties(includeNested = false)
            .withPublicOrDefaultModifier()
            .withoutAnnotationNamed(JvmSynthetic::class.java.simpleName)
            .assertTrue(strict = true) { it.hasAnnotationOf<JvmStatic>() }
    }
}
