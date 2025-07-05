package io.github.freya022.botcommands.framework

import io.github.classgraph.AnnotationClassRef
import io.github.classgraph.AnnotationInfo
import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.framework.utils.createTest
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.mockk.mockkClass
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import kotlin.reflect.KClass
import kotlin.test.assertSame

object AutoConfigurationOverrideTest {

    private lateinit var autoconfiguredTypes: Set<KClass<*>>

    @JvmStatic
    @BeforeAll
    fun setup() {
        val autoconfiguredTypes = hashSetOf<KClass<*>>()

        ClassGraph()
            .acceptPackages("io.github.freya022.botcommands", "dev.freya02.botcommands")
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use { scan ->
                val autoConfigurationClasses = scan.getClassesWithAnnotation(InternalAutoConfiguration::class.java)
                autoConfigurationClasses.forEach { autoConfigurationClass ->
                    @Suppress("UNCHECKED_CAST")
                    fun AnnotationInfo.getRequiredMissingBeans(): List<KClass<*>> {
                        val requiredMissingBeans = parameterValues["value"].value as Array<*>
                        return requiredMissingBeans.map { (it as AnnotationClassRef).loadClass().kotlin }
                    }

                    autoConfigurationClass
                        .getAnnotationInfo(ConditionalOnMissingService::class.java)
                        ?.also { conditionalOnMissingBean ->
                            autoconfiguredTypes += conditionalOnMissingBean.getRequiredMissingBeans()
                        }

                    autoConfigurationClass.declaredMethodInfo.forEach { method ->
                        method
                            .getAnnotationInfo(ConditionalOnMissingService::class.java)
                            ?.also { conditionalOnMissingBean ->
                                autoconfiguredTypes += conditionalOnMissingBean.getRequiredMissingBeans()
                            }
                    }
                }
            }

        this.autoconfiguredTypes = autoconfiguredTypes
    }

    @ParameterizedTest
    @FieldSource("autoconfiguredTypes")
    fun `Can override auto configured services`(autoconfiguredType: KClass<Any>) {
        val expected = mockkClass(autoconfiguredType, relaxed = true)
        val context = BotCommands.createTest {
            services {
                registerServiceSupplier(primaryType = autoconfiguredType) { expected }
            }
        }

        val actual = assertDoesNotThrow { context.getService(autoconfiguredType) }
        assertSame(expected, actual)
    }
}
