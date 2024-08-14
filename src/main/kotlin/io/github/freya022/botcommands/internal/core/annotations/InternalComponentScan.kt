package io.github.freya022.botcommands.internal.core.annotations

import org.springframework.context.annotation.ComponentScan
import org.springframework.core.annotation.AliasFor

@ComponentScan
internal annotation class InternalComponentScan(
    @get:AliasFor(annotation = ComponentScan::class)
    vararg val basePackages: String
)