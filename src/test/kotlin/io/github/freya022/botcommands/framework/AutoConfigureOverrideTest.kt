package io.github.freya022.botcommands.framework

import io.github.classgraph.AnnotationClassRef
import io.github.classgraph.AnnotationInfo
import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.framework.utils.createTest
import io.mockk.mockkClass
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import kotlin.reflect.KClass
import kotlin.test.assertSame

// We don't have Spring on the runtime classpath
private const val AUTO_CONFIGURATION_ANNOTATION_NAME = "org.springframework.boot.autoconfigure.AutoConfiguration"
private const val CONDITIONAL_ON_MISSING_BEAN_ANNOTATION_NAME = "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean"

object AutoConfigureOverrideTest {

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
                val autoConfigurationClasses = scan.getClassesWithAnnotation(AUTO_CONFIGURATION_ANNOTATION_NAME)
                autoConfigurationClasses.forEach { autoConfigurationClass ->
                    @Suppress("UNCHECKED_CAST")
                    fun AnnotationInfo.getRequiredMissingBeans(): List<KClass<*>> {
                        val requiredMissingBeans = parameterValues["value"].value as Array<*>
                        return requiredMissingBeans.map { (it as AnnotationClassRef).loadClass().kotlin }
                    }

                    autoConfigurationClass
                        .getAnnotationInfo(CONDITIONAL_ON_MISSING_BEAN_ANNOTATION_NAME)
                        ?.also { conditionalOnMissingBean ->
                            autoconfiguredTypes += conditionalOnMissingBean.getRequiredMissingBeans()
                        }

                    autoConfigurationClass.declaredMethodInfo.forEach { method ->
                        method
                            .getAnnotationInfo(CONDITIONAL_ON_MISSING_BEAN_ANNOTATION_NAME)
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
        val expected = mockkClass(autoconfiguredType)
        val context = BotCommands.createTest {
            services {
                registerServiceSupplier(primaryType = autoconfiguredType) { expected }
            }
        }

        val actual = assertDoesNotThrow { context.getService(autoconfiguredType) }
        assertSame(expected, actual)
    }
}