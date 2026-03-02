package dev.freya02.botcommands.restarter.api

import dev.freya02.botcommands.restarter.api.BotCommandsRestarter.initialize
import dev.freya02.botcommands.restarter.api.annotations.ExperimentalRestartApi
import dev.freya02.botcommands.restarter.api.config.RestarterConfigBuilder
import dev.freya02.botcommands.restarter.api.exceptions.ImmediateRestartException
import dev.freya02.botcommands.restarter.internal.Restarter
import io.github.freya022.botcommands.api.ReceiverConsumer

/**
 * Entry point for the "hot restart" feature.
 *
 * @see initialize
 */
@ExperimentalRestartApi
object BotCommandsRestarter {

    /**
     * Enables hot restarting for this application. All changes to your code will restart the application.
     *
     * This method will intentionally throw [ImmediateRestartException] on the first run,
     * if it is caught, you must rethrow it.
     *
     * It is recommended to run this function as soon as possible to avoid running the same code twice on startup.
     *
     * **Note:** If you configure your logger programmatically, it must be done before calling this function.
     *
     * @param args          The program arguments, they will be passed back to the main function upon restarting
     * @param configBuilder To further configure the feature, this will only run once per application
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(args: Array<out String>, configBuilder: ReceiverConsumer<RestarterConfigBuilder> = {}) {
        if (!Restarter.isInitialized) {
            // 1st restart
            val config = RestarterConfigBuilder.create(args)
                .apply(configBuilder)
                .build()
            Restarter.initialize(config)
        }

        // After 1st restart
    }
}
