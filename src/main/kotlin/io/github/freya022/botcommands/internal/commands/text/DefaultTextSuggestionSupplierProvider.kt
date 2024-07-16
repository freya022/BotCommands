package io.github.freya022.botcommands.internal.commands.text

import info.debatty.java.stringsimilarity.NormalizedLevenshtein
import io.github.freya022.botcommands.api.commands.text.TextSuggestionSupplier
import io.github.freya022.botcommands.api.commands.text.TopLevelTextCommandInfo
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServiceTypes
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.classRef
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.math.min

@BService
@Configuration
internal open class DefaultTextSuggestionSupplierProvider {
    @Bean
    @ConditionalOnMissingBean(TextSuggestionSupplier::class)
    @BService
    @ConditionalService(ExistingSupplierChecker::class)
    internal open fun defaultTextSuggestionSupplier(): TextSuggestionSupplier = object : TextSuggestionSupplier {
        private val normalizedLevenshtein = NormalizedLevenshtein()

        override fun getSuggestions(
            topLevelName: String,
            candidates: List<TopLevelTextCommandInfo>,
        ): Collection<TopLevelTextCommandInfo> {
            // Keep strings that have more than 90% matching on same-length strings
            val partialMatches =
                candidates.filter { normalizedLevenshtein.partialSimilarity(it.name, topLevelName) > 0.9 }

            val matches = buildList {
                partialMatches.forEach {
                    val similarity = normalizedLevenshtein.similarity(it.name, topLevelName)
                    if (similarity > 0.42) {
                        this += it to (1 - similarity)
                    }
                }
            }

            return matches.sortedBy { it.second }.map { it.first }
        }

        // https://stackoverflow.com/a/53756045
        // Similarity between the substring of the shortest length
        private fun NormalizedLevenshtein.partialSimilarity(s1: String, s2: String): Double {
            val minLength = min(s1.length, s2.length)
            val cutS1 = s1.take(minLength)
            val cutS2 = s2.take(minLength)

            return similarity(cutS1, cutS2)
        }
    }

    internal object ExistingSupplierChecker : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            // Try to get TextSuggestionSupplier interfaced services, except ours
            // If empty, then the user didn't provide one, in which case we can allow
            //Won't take DefaultTextSuggestionSupplier into account
            val suppliers = serviceContainer.getInterfacedServiceTypes<TextSuggestionSupplier>()
            if (suppliers.isNotEmpty())
                return "An user supplied ${classRef<TextSuggestionSupplier>()} is already active (${suppliers.first().simpleNestedName})"

            return null
        }
    }
}
