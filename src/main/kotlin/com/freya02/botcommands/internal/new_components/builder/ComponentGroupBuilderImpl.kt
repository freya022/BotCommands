package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.new_components.builder.ComponentGroupBuilder
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import com.freya02.botcommands.internal.new_components.new.EphemeralTimeout
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import kotlinx.datetime.Clock
import kotlin.time.Duration

//TODO change constraints to allow ephemeral groups
internal class ComponentGroupBuilderImpl internal constructor(internal val _componentIds: List<Int>) : ComponentGroupBuilder {
    override var oneUse: Boolean = false
    override var timeout: ComponentTimeout? = null

    override val componentIds: List<String> by lazy {
        _componentIds.map { it.toString() }
    }

    override fun oneUse() = this.also { oneUse = true }

    @JvmSynthetic
    override fun timeout(timeout: Duration, handler: suspend () -> Unit): ComponentGroupBuilder = this.also {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, handler)
    }

    @JvmSynthetic
    override fun timeout(timeout: Duration, handlerName: String, vararg args: Any?): ComponentGroupBuilder = this.also {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, handlerName, args)
    }
}