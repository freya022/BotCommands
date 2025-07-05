package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import net.dv8tion.jda.api.JDA

/**
 * Fired when [JDA] connects to the gateway for the first time.
 *
 * Remember that at this point, the JDA instance is not fully initialized.
 *
 * Listeners of this event can use the [BLOCKING][BEventListener.RunMode.BLOCKING] mode,
 * which will prevent JDA from going any further until the listener returns,
 * including from logging in other shards.
 *
 * This makes this event suited for usages when you need to run logic after loading all services,
 * and after the token was validated, but before JDA actual logs in the gateway and starts receiving events.
 */
class PreFirstGatewayConnectEvent internal constructor(context: BContext, val jda: JDA) : BEvent(context)