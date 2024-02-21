package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.data.PersistentTimeout
import kotlinx.datetime.Clock
import kotlin.time.Duration

internal class PersistentTimeoutableComponentImpl<T : IPersistentTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IPersistentTimeoutableComponent<T> {

    override var timeout: PersistentTimeout = PersistentTimeout.create(Clock.System.now() + Components.defaultTimeout)
        private set

    override fun timeout(timeout: Duration): T = instance.also {
        this.timeout = PersistentTimeout.create(Clock.System.now() + timeout)
    }

    override fun timeout(timeout: Duration, handlerName: String, vararg data: Any?): T = instance.also {
        this.timeout = PersistentTimeout.create(Clock.System.now() + timeout, handlerName, data.toList())
    }
}