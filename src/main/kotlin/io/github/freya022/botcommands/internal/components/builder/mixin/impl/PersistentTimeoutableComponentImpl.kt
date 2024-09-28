package io.github.freya022.botcommands.internal.components.builder.mixin.impl

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.builder.BuilderInstanceHolderImpl
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.IPersistentTimeoutableComponentMixin
import io.github.freya022.botcommands.internal.components.data.timeout.PersistentTimeout
import io.github.freya022.botcommands.internal.utils.Checks
import kotlin.time.Duration

internal class PersistentTimeoutableComponentImpl<T : IPersistentTimeoutableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IPersistentTimeoutableComponentMixin<T> {

    override var timeoutDuration: Duration? = null
        private set
    override var timeout: PersistentTimeout? = null
        private set

    override var resetTimeoutOnUse: Boolean = false

    override fun resetTimeoutOnUse(resetTimeoutOnUse: Boolean): T = applyInstance {
        this.resetTimeoutOnUse = resetTimeoutOnUse
    }

    override fun noTimeout(): T = applyInstance {
        this.timeoutDuration = null
        this.timeout = null
    }

    override fun timeout(timeout: Duration): T = applyInstance {
        Checks.checkFinite(timeout, "timeout")
        Checks.checkFitInt(timeout, "timeout")

        this.timeoutDuration = timeout
        this.timeout = null
    }

    override fun timeout(timeout: Duration, handlerName: String, vararg data: Any?): T = applyInstance {
        Checks.checkFinite(timeout, "timeout")
        Checks.checkFitInt(timeout, "timeout")

        this.timeoutDuration = timeout
        this.timeout = PersistentTimeout.create(handlerName, data.toList())
    }
}