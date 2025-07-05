package io.github.freya022.botcommands.arch

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.combined.KoClassAndInterfaceDeclaration
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.ext.provider.hasAnnotationOf
import com.lemonappdev.konsist.api.provider.KoPackageProvider
import com.lemonappdev.konsist.api.provider.KoReturnProvider
import com.lemonappdev.konsist.api.verify.assertTrue
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService
import kotlin.test.Test

class AutoConfigurationArchTest {

    private val autoConfigurationClasses = Konsist.scopeFromProduction()
        .classes()
        .filter { it.hasAnnotationOf<InternalAutoConfiguration>() }

    private val autoConfigurationFactories = autoConfigurationClasses
        .functions()
        .filter { it.hasAnnotationOf<InternalAutoConfigurationBeanService>() }

    @Test
    fun `Auto configuration class's name ends with 'AutoConfiguration'`() {
        autoConfigurationClasses.assertTrue(strict = true) { it.hasNameEndingWith("AutoConfiguration") }
    }

    @Test
    fun `Auto configuration classes's package is the returned type's but internal, and ends with 'autoconfigure'`() {
        // Example:
        // If factory return a type annotated with [[InterfacedService]] (`api.X.Y.Z`),
        // and declaring class is [[InternalAutoConfiguration]],
        // then the package must be `internal.X.Y.Z.autoconfigure`

        autoConfigurationClasses
            .functions()
            // Returned instance is interfaced service
            .filter { it.returnTypeAsInterfacedServiceOrNull() != null }
            .assertTrue(strict = true) { function ->
                val autoConfigurationClass = function.containingDeclaration as KoClassDeclaration
                val autoConfigurationSubpackage = autoConfigurationClass.packageName.substringAfter(".internal.")

                val interfacedServiceDeclaration = function.returnTypeAsInterfacedServiceOrNull()!! // Already filtered
                val returnTypeSubpackage = interfacedServiceDeclaration.packageName.substringAfter(".api.")

                autoConfigurationSubpackage == "${returnTypeSubpackage}.autoconfigure"
            }
    }

    @Test
    fun `Auto configuration factories returns an @InterfacedService`() {
        autoConfigurationFactories.assertTrue(strict = true) { function ->
            function.returnTypeAsInterfacedServiceOrNull() != null
        }
    }

    @Test
    fun `Auto configuration factory names must be the camelCase of the returned service type`() {
        autoConfigurationFactories.assertTrue(strict = true) { function ->
            function.name == function.returnType?.name?.replaceFirstChar(Char::lowercase)
        }
    }

    @Test
    fun `Auto configuration factories must have @ConditionalOnMissingService`() {
        autoConfigurationFactories.assertTrue(strict = true) { function ->
            val conditionalOnMissingService = function.annotations
                .find { it.fullyQualifiedName == ConditionalOnMissingService::class.java.name }
                ?: return@assertTrue false

            val requiredMissingService = conditionalOnMissingService.arguments[0].value!!.substringBefore("::class")

            function.returnType!!.name == requiredMissingService
        }
    }

    @Test
    fun `Auto configuration factories must be public`() {
        autoConfigurationFactories.assertTrue(strict = true) { function ->
            function.hasPublicOrDefaultModifier
        }
    }

    private fun KoReturnProvider.returnTypeAsInterfacedServiceOrNull(): KoClassAndInterfaceDeclaration? {
        return returnType?.sourceDeclaration?.asInterfaceDeclaration()
            ?.takeIf { it.hasAnnotationOf<InterfacedService>() }
    }

    private val KoPackageProvider.packageName get() = packagee!!.name
}
