package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.internal.core.db.RequiresDatabase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

//TODO when BC supports reading inherited annotations,
// add @ConditionalService(Components.InstantiationChecker::class)
@ConditionalOnProperty("botcommands.components.enable")
@RequiresDatabase
annotation class RequiresComponents