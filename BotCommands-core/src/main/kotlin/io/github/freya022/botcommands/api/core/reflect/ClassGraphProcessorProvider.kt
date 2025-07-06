package io.github.freya022.botcommands.api.core.reflect

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.reflect.annotations.ExperimentalReflectionApi
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor

@ExperimentalReflectionApi
interface ClassGraphProcessorProvider {

    fun getProcessors(config: BConfig): Collection<ClassGraphProcessor>
}
