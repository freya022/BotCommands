package io.github.freya022.botcommands.test.commands.slash

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.builder.timeout
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import kotlin.time.Duration.Companion.days
import kotlin.time.measureTime

private const val amount = 5000

@Command
@RequiresComponents
class SlashGenerateComponents(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "generate_components")
    suspend fun onSlashGenerateComponents(event: GuildSlashEvent) {
        event.deferReply(true).queue()

        val duration = measureTime {
            repeat(amount / 3) {
                buttons.primary("persistent").persistent {
                    timeout(1.days, ::onPersistentTimeout)
                }
            }

            repeat(amount / 3) {
                buttons.primary("ephemeral").ephemeral {
                    timeout(1.days) {
                        println("Ephemeral timeout")
                    }
                }
            }

            val buttonsWithoutTimeout = hashSetOf<Button>()
            repeat((amount / 3).coerceAtLeast(50 * 10)) {
                buttonsWithoutTimeout += buttons.primary("ephemeral").ephemeral {
                    noTimeout()
                }
            }

            repeat(50) {
                val groupMembers = hashSetOf<Button>()
                while (groupMembers.size != 10) {
                    groupMembers += buttonsWithoutTimeout.random()
                }
                buttonsWithoutTimeout -= groupMembers //Removes used IDs

                buttons.group(*groupMembers.toTypedArray()).persistent {
                    timeout(1.days)
                }
            }
        }

        event.hook.sendMessage("Done in $duration").queue()
    }

    @ComponentTimeoutHandler
    fun onPersistentTimeout(data: ComponentTimeoutData) {
        println("Persistent timeout")
    }
}