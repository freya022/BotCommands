package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

//TODO when BC supports reading inherited annotations,
// add @ConditionalService(Components.InstantiationChecker::class)
@ConditionalOnProperty("botcommands.components.enable")
@RequiresDatabase
annotation class RequiresComponents