package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.*
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import kotlinx.datetime.Clock
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

@OptIn(ExperimentalTime::class)
internal class PersistentButtonBuilder internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : ButtonBuilder<PersistentButtonBuilder>(componentController, style) {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT

    //TODO use more accurate types in impl classes
    override var timeout: ComponentTimeout? = null
        private set
    override var handler: ComponentHandler? = null
        private set

    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg args: Any?): PersistentButtonBuilder =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *args)

    @JvmSynthetic
    fun timeout(timeout: Duration, handlerName: String, vararg args: Any?): PersistentButtonBuilder = this.also {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, handlerName, args)
    }

    fun bindTo(handlerName: String, vararg data: Any?): PersistentButtonBuilder = this.also { handler = PersistentHandler(handlerName, data) }
}