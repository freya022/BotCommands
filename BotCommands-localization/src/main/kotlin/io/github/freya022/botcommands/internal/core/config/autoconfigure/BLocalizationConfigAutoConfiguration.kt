package io.github.freya022.botcommands.internal.core.config.autoconfigure

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.core.config.getConfigOrNull
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getService
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * This is only giving a blank [BLocalizationConfig] when the built-in DI is used,
 * Spring doesn't need that as it always creates the configuration since there is no condition.
 *
 * But in built-in DI, we need to register one to simplify things when no localization is configured.
 */
@BService
@Conditional(DenyCondition::class) // Disable it for Spring
@ConditionalService(LocalizationConfigCondition::class) // Disable it if config exists
internal open class BLocalizationConfigAutoConfiguration internal constructor() {

    @BService
    fun localizationConfig(): BLocalizationConfig {
        return object : BLocalizationConfig {
            override val responseBundles: Set<String> get() = emptySet()
        }
    }
}

internal object DenyCondition : Condition {

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return false
    }
}

internal class LocalizationConfigCondition : ConditionalServiceChecker {

    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
        if (serviceContainer.getService<BConfig>().getConfigOrNull<BLocalizationConfig>() == null) {
            return null
        }

        return "Localization config has been registered"
    }
}
