package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.internal.core.ClassPathProcessor
import io.github.freya022.botcommands.internal.core.ClassPathProcessorProvider

internal class CommandsPresenceCheckerProvider : ClassPathProcessorProvider {
    override fun getProcessors(config: BConfig): Collection<ClassPathProcessor> {
        return listOf(CommandsPresenceChecker())
    }
}
