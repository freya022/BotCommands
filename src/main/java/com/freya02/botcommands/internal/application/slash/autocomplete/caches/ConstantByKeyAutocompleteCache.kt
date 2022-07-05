package com.freya02.botcommands.internal.application.slash.autocomplete.caches

import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.application.slash.autocomplete.CompositeAutocompletionKey
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import kotlin.time.Duration.Companion.minutes

//private val LOGGER = Logging.getLogger()

internal class ConstantByKeyAutocompleteCache(
    private val handler: AutocompleteHandler
) : BaseAutocompleteCache(handler.autocompleteInfo) {
    private val cache: Cache<CompositeAutocompletionKey, List<Command.Choice>>
    private val maxWeight: Long = handler.autocompleteInfo.cacheSize * 1024
    private val lock = Mutex()

    init {
        cache = Caffeine.newBuilder()
//            .evictionListener { key: CompositeAutocompletionKey?, value: List<Command.Choice>?, cause: RemovalCause ->
//                if (key != null && value != null) {
//                    LOGGER.trace("Evicted autocomplete key '{}', of size {} for cause {}, maximum weight: {}", key, getEntrySize(key, value), cause.name, maxWeight)
//                }
//            }
            .maximumWeight(maxWeight)
            .weigher { k: CompositeAutocompletionKey, v: List<Command.Choice> -> getEntrySize(k, v) }
            .build()
    }

    //Weight by the sum of the choice value lengths
    private fun getEntrySize(key: CompositeAutocompletionKey, choices: List<Command.Choice>): Int =
        key.length() + choices.sumOf { c -> c.name.length + c.asString.length }

    override suspend fun retrieveAndCall(
        event: CommandAutoCompleteInteractionEvent,
        valueComputer: suspend (CommandAutoCompleteInteractionEvent) -> List<Command.Choice>
    ): List<Command.Choice> {
        val compositeKey = getCompositeKey(handler, event)

        lock.withLock(this) {
            return cache.getIfPresent(compositeKey)
                ?: withTimeout(1.minutes) {
                    return@withTimeout valueComputer(event).also { computedChoices ->
                        cache.put(compositeKey, computedChoices)
                    }
                }
        }
    }

    override fun invalidate() {
        cache.invalidateAll()
    }
}