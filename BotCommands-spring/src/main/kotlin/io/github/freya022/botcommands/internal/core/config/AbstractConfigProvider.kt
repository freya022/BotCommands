package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.BConfigurer
import kotlin.collections.forEach

internal abstract class AbstractConfigProvider {
    protected fun <T : Any> T.configure(configurers: List<BConfigurer<T>>) = apply {
        configurers.forEach { configConfigurer -> configConfigurer.configure(this) }
    }
}
