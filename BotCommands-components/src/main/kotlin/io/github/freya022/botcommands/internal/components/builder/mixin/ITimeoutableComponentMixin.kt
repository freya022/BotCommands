package io.github.freya022.botcommands.internal.components.builder.mixin

import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.api.components.builder.ITimeoutableComponent
import io.github.freya022.botcommands.internal.components.builder.BuilderInstanceHolder
import io.github.freya022.botcommands.internal.components.data.timeout.ComponentTimeout
import kotlin.time.Duration

internal interface ITimeoutableComponentMixin<T : ITimeoutableComponent<T>> : ITimeoutableComponent<T>,
                                                                              BuilderInstanceHolder<T> {

    val timeoutDuration: Duration?
    val timeout: ComponentTimeout?
}

internal interface IPersistentTimeoutableComponentMixin<T : IPersistentTimeoutableComponent<T>> :
        IPersistentTimeoutableComponent<T>,
        ITimeoutableComponentMixin<T>

internal interface IEphemeralTimeoutableComponentMixin<T : IEphemeralTimeoutableComponent<T>> :
        IEphemeralTimeoutableComponent<T>,
        ITimeoutableComponentMixin<T>
