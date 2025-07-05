package io.github.freya022.botcommands.internal.components.serialization.autoconfigure

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.freya022.botcommands.api.components.serialization.GlobalComponentDataSerializer
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.serialization.readValue
import io.github.freya022.botcommands.api.components.serialization.writeValueAsComponentData
import io.github.freya022.botcommands.api.core.reflect.KotlinTypeToken
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper

internal object DefaultGlobalComponentDataSerializer : GlobalComponentDataSerializer {

    private val mapper = jacksonObjectMapper()

    override fun serialize(parameter: ParameterWrapper, obj: Any): SerializedComponentData {
        return mapper.writeValueAsComponentData(obj)
    }

    override fun deserialize(parameter: ParameterWrapper, data: SerializedComponentData): Any {
        @Suppress("UNCHECKED_CAST")
        return mapper.readValue(data, parameter.typeToken as KotlinTypeToken<Any>)
    }
}
