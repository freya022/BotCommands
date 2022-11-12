package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.data.DataEntityTimeout
import com.freya02.botcommands.internal.data.DataStoreService
import com.freya02.botcommands.internal.data.PartialDataEntity
import com.freya02.botcommands.internal.new_components.*
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.TimeUnit

internal typealias ComponentTimeoutInfo = DataEntityTimeout

class ButtonBuilder internal constructor(private val dataStore: DataStoreService, private val ephemeralHandlers: EphemeralHandlers) {
    private var oneUse: Boolean = false
    private var constraints: InteractionConstraints = InteractionConstraints()
    private var timeoutInfo: ComponentTimeoutInfo? = null
    private var handler: ComponentHandler? = null

    fun oneUse(): ButtonBuilder = this.also { oneUse = true }

    fun constraints(block: InteractionConstraints.() -> Unit): ButtonBuilder = this.also { constraints.apply(block) }

    @JvmOverloads
    fun timeout(amount: Long, unit: TimeUnit, timeoutHandlerName: String? = null): ButtonBuilder =
        this.also { timeoutInfo = ComponentTimeoutInfo(amount, unit, timeoutHandlerName) }

    fun bindTo(handlerName: String, vararg data: Any?): ButtonBuilder = this.also { handler = PersistentHandler(handlerName, data) }

    fun bindTo(handler: (ButtonEvent) -> Unit): ButtonBuilder = this.also { it.handler = EphemeralHandler(handler) }

    fun build(label: String): Button = build(label, null)
    fun build(emoji: Emoji): Button = build(null, emoji)
    fun build(label: String?, emoji: Emoji?): Button {
        val data = PersistentComponentData(ComponentType.BUTTON, oneUse, constraints, timeoutInfo, handler as PersistentHandler)
        val entityTimeout = timeoutInfo?.let { DataEntityTimeout(it.duration, NewComponentsListener.TIMEOUT_HANDLER_NAME) }
        return runBlocking {
            val id = dataStore.putData(PartialDataEntity.ofPersistent(data, entityTimeout))
            return@runBlocking Button.of(ButtonStyle.PRIMARY, id, label, emoji)
        }
    }
}

suspend fun Button.await(): ButtonEvent = TODO()