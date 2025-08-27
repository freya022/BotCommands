package io.github.freya022.botcommands.arch

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoTypeArgumentDeclaration
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withPublicOrDefaultModifier
import com.lemonappdev.konsist.api.ext.list.withParameter
import com.lemonappdev.konsist.api.ext.list.withoutAnnotationNamed
import com.lemonappdev.konsist.api.ext.provider.hasAnnotationOf
import com.lemonappdev.konsist.api.provider.*
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertNotEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import io.github.freya022.botcommands.internal.core.annotations.SkipJavaReflectionOverload
import kotlin.test.Test

class JavaInteropTest {

    @Test
    fun `Check all functions accepting kotlin-reflect instances have Java equivalents`() {
        Konsist.scopeFromProduction()
            .classes(includeNested = true)
            .filter { it.packagee!!.name.contains("api") }
            .functions(includeNested = true)
            .withoutAnnotationNamed(SkipJavaReflectionOverload::class.java.simpleName, JvmSynthetic::class.java.simpleName)
            .also { it.assertNotEmpty(strict = true) }
            .withParameter {
                it.getAllTypes().any { type ->
                    val typeSource = (type as KoSourceDeclarationProvider).sourceDeclaration as? KoPackageProvider ?: return@any false

                    typeSource.packagee!!.hasNameStartingWith("kotlin.reflect")
                }
            }
            .assertEmpty(strict = true)
    }

    private fun KoNonNullableTypeProvider.getAllTypes(): List<KoBaseProvider> {
        fun KoTypeArgumentProvider.allArgumentTypes(): List<KoTypeArgumentDeclaration> {
            val args = typeArguments ?: return emptyList()
            return args + args.flatMap { it.allArgumentTypes() }
        }

        return listOf(this.type) + this.type.allArgumentTypes()
    }

    @Test
    fun `Check all object functions have @JvmStatic`() {
        Konsist.scopeFromProduction()
            .objects(includeNested = true)
            .filter { it.packagee!!.name.contains("api") }
            .functions(includeNested = true)
            .withPublicOrDefaultModifier()
            .withoutAnnotationNamed(JvmSynthetic::class.java.simpleName)
            .assertTrue(strict = true) { it.hasAnnotationOf<JvmStatic>() }
    }
}
