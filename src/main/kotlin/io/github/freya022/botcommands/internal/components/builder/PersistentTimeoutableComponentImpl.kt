package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.data.PersistentTimeout
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.freya022.botcommands.internal.utils.toTimestampIfFinite
import kotlin.time.Duration

internal class PersistentTimeoutableComponentImpl<T : IPersistentTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IPersistentTimeoutableComponent<T> {

    override var timeout: PersistentTimeout? = Components.defaultTimeout.toTimestampIfFinite()?.let { PersistentTimeout.create(it) }
        private set

    override fun noTimeout(): T = instance.also {
        timeout = null
    }

    override fun timeout(timeout: Duration): T = instance.also {
        val expirationTimestamp = timeout.toTimestampIfFinite() ?: throwUser("Timeout must be positive and finite")
        this.timeout = PersistentTimeout.create(expirationTimestamp)
    }

    override fun timeout(timeout: Duration, handlerName: String, vararg data: Any?): T = instance.also {
        val expirationTimestamp = timeout.toTimestampIfFinite() ?: throwUser("Timeout must be positive and finite")
        this.timeout = PersistentTimeout.create(expirationTimestamp, handlerName, data.toList())
    }
}