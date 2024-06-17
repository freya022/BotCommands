package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.data.PersistentTimeout
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.toTimestampIfFinite
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class PersistentTimeoutableComponentImpl<T : IPersistentTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IPersistentTimeoutableComponent<T> {
    override var expiresAt: Instant? = null
        private set
    override var timeout: PersistentTimeout? = null
        private set

    override fun noTimeout(): T = instance.also {
        this.expiresAt = null
        this.timeout = null
    }

    override fun timeout(timeout: Duration): T = instance.also {
        this.expiresAt = timeout.toTimestampIfFinite() ?: throwArgument("Timeout must be positive and finite")
        this.timeout = null
    }

    override fun timeout(timeout: Duration, handlerName: String, vararg data: Any?): T = instance.also {
        this.expiresAt = timeout.toTimestampIfFinite() ?: throwArgument("Timeout must be positive and finite")
        this.timeout = PersistentTimeout.create(handlerName, data.toList())
    }
}