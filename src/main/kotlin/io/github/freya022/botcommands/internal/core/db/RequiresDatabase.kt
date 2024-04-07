package io.github.freya022.botcommands.internal.core.db

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

//TODO when BC supports reading inherited annotations,
// add @Dependencies(ConnectionSupplier::class)
@ConditionalOnProperty("botcommands.database.enable")
annotation class RequiresDatabase