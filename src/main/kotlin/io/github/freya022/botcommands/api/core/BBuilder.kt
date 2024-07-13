package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlinx.coroutines.cancel
import net.dv8tion.jda.api.events.session.ShutdownEvent
import kotlin.time.Duration.Companion.minutes

/**
 * Entry point for the BotCommands framework.
 *
 * The only requirement for a basic bot is a service extending [JDAService],
 * learn more on [the wiki](https://freya022.github.io/BotCommands/3.X/setup/getting-started/#creating-a-jdaservice).
 *
 * @see BService @BService
 * @see InterfacedService @InterfacedService
 * @see Command @Command
 */
@Suppress("DEPRECATION")
@Deprecated("Replaced with the BotCommands entry point")
class BBuilder private constructor() {
    /**
     * Entry point for the BotCommands framework.
     *
     * @see BBuilder
     */
    companion object {
        /**
         * Creates a new instance of the framework.
         *
         * @see BBuilder
         */
        @Deprecated(
            "Replaced with the BotCommands entry point",
            replaceWith = ReplaceWith(
                expression = "BotCommands.create(configConsumer)",
                imports = ["io.github.freya022.botcommands.api.core.BotCommands"]
            )
        )
        @JvmStatic
        @JvmName("newBuilder")
        fun newBuilderJava(configConsumer: ReceiverConsumer<BConfigBuilder>): BContext {
            return BotCommands.create(configConsumer = configConsumer)
        }

        /**
         * Creates a new instance of the framework.
         *
         * @see BBuilder
         */
        @Deprecated(
            "Replaced with the BotCommands entry point",
            replaceWith = ReplaceWith(
                expression = "BotCommands.create(manager, configConsumer)",
                imports = ["io.github.freya022.botcommands.api.core.BotCommands"]
            )
        )
        @JvmSynthetic
        fun newBuilder(manager: CoroutineEventManager = getDefaultManager(), configConsumer: ReceiverConsumer<BConfigBuilder>): BContext {
            return BotCommands.create(manager, configConsumer)
        }

        private fun getDefaultManager(): CoroutineEventManager {
            val scope = getDefaultScope()
            return CoroutineEventManager(scope, 1.minutes).apply {
                listener<ShutdownEvent> {
                    scope.cancel()
                }
            }
        }
    }
}
