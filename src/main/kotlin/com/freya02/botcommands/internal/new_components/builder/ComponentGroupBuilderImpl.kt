package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.new_components.builder.ComponentGroupBuilder
import com.freya02.botcommands.api.new_components.builder.IEphemeralTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.IPersistentTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.IUniqueComponent
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import kotlinx.datetime.Clock
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

//TODO should probably separate this into one ephemeral and one persistent class
// Currently it would be awkward if the ephemeral handlers got deleted for a persistent group
internal class ComponentGroupBuilderImpl internal constructor(internal val _componentIds: List<Int>) :
    ComponentGroupBuilder,
    IPersistentTimeoutableComponent by PersistentTimeoutableComponentImpl(),
    IEphemeralTimeoutableComponent by EphemeralTimeoutableComponentImpl(),
    IUniqueComponent by UniqueComponentImpl() {

    override var timeout: ComponentTimeout? = null // Can be both ephemeral and persistent...

    override val componentIds: List<String> by lazy {
        _componentIds.map { it.toString() }
    }

    override fun timeout(timeout: Long, timeoutUnit: TimeUnit) {
        super<IPersistentTimeoutableComponent>.timeout(timeout, timeoutUnit)
    }

    override fun timeout(timeout: Duration) {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, null, emptyArray())
    }
}