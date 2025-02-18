package io.github.freya022.botcommands.internal.components.serialization

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.freya022.botcommands.api.components.serialization.GlobalComponentDataSerializer
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.serialization.readValue
import io.github.freya022.botcommands.api.components.serialization.writeValueAsComponentData
import io.github.freya022.botcommands.api.core.reflect.KotlinTypeToken
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServiceTypes
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@BService
@Configuration
internal open class DefaultGlobalComponentDataSerializerProvider {

    @Bean
    @ConditionalOnMissingBean(GlobalComponentDataSerializer::class)
    @BService
    @ConditionalService(ExistingInstanceChecker::class)
    internal open fun defaultGlobalComponentDataSerializer(): GlobalComponentDataSerializer {
        return DefaultGlobalComponentDataSerializer
    }

    private object DefaultGlobalComponentDataSerializer : GlobalComponentDataSerializer {
        private val mapper = jacksonObjectMapper()

        override fun serialize(parameter: ParameterWrapper, obj: Any): SerializedComponentData {
            return mapper.writeValueAsComponentData(obj)
        }

        override fun deserialize(parameter: ParameterWrapper, data: SerializedComponentData): Any {
            @Suppress("UNCHECKED_CAST")
            return mapper.readValue(data, parameter.typeToken as KotlinTypeToken<Any>)
        }
    }

    internal object ExistingInstanceChecker : ConditionalServiceChecker {

        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            // Does not include default instance as it uses this checker
            val existingType = serviceContainer.getInterfacedServiceTypes<GlobalComponentDataSerializer>()
            if (existingType.size > 1) throwInternal("Cannot have more than 1 ${classRef<GlobalComponentDataSerializer>()}")

            if (existingType.size == 1) {
                return "Disabling default ${classRef<GlobalComponentDataSerializer>()}, using ${existingType.first().shortQualifiedName} instead"
            }

            return null
        }
    }
}