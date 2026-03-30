package io.github.freya022.botcommands.internal.components.builder.mixin.impl

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.builder.BuilderInstanceHolderImpl
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.IPersistentTimeoutableComponentMixin
import io.github.freya022.botcommands.internal.components.data.timeout.PersistentTimeout
import io.github.freya022.botcommands.internal.utils.Checks
import io.github.freya022.botcommands.internal.utils.takeIfFinite
import kotlin.time.Duration

internal class PersistentTimeoutableComponentImpl<T : IPersistentTimeoutableComponent<T>> internal constructor(
    private val context: BContext,
    private val componentType: ComponentType,
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IPersistentTimeoutableComponentMixin<T> {

    override var timeoutDuration: Duration? = Components.defaultPersistentTimeout?.takeIfFinite()
    override var timeout: PersistentTimeout? = null

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

    override fun timeout(timeout: Duration, handlerName: String, data: List<Any?>): T = applyInstance {
        Checks.checkFinite(timeout, "timeout")
        Checks.checkFitInt(timeout, "timeout")

        this.timeoutDuration = timeout
        this.timeout = PersistentTimeout.create(context, componentType, handlerName, data)
    }
}