package io.github.freya022.botcommands.api.core.db.annotations

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

//TODO when BC supports reading inherited annotations,
// add @Dependencies(ConnectionSupplier::class)
@ConditionalOnProperty("botcommands.database.enable")
annotation class RequiresDatabase