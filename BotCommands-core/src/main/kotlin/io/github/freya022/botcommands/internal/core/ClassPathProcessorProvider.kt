package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.config.BConfig

interface ClassPathProcessorProvider {

    fun getProcessors(config: BConfig): Collection<ClassPathProcessor>
}
