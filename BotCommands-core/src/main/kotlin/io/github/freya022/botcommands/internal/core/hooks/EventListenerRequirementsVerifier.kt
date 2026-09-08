package io.github.freya022.botcommands.internal.core.hooks

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.BEvent
import io.github.freya022.botcommands.api.core.events.BGenericEvent
import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirementsProvider
import io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwState
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.reflect.KFunction

internal interface EventListenerRequirementsVerifier {

    fun verifyFor(
        function: KFunction<*>,
        locallySkipIntentChecks: Boolean,
        locallySkippedIntents: Set<GatewayIntent>,
        eventErasure: Class<*>,
    ): Boolean
}

@OptIn(ExperimentalCustomEvents::class)
@BService
@ServiceType(EventListenerRequirementsVerifier::class)
internal class EventListenerRequirementsVerifierImpl(
    private val config: BConfig,
    private val jdaService: JDAService,
    private val customEventRequirementsProviders: List<CustomEventRequirementsProvider>,
) : EventListenerRequirementsVerifier {

    private companion object {

        private val logger = KotlinLogging.logger { }
    }

    init {
        // Try to enforce requirement providers to only return requirements for events they truly know about
        // in other words, forbid implementations from returning fake requirements.
        for (requirementsProvider in customEventRequirementsProviders) {
            val eventRequirements = requirementsProvider.get(FakeEvent::class.java)
            check(eventRequirements.areUnknown()) {
                "Custom event requirement providers are required to return 'CustomEventRequirements.unknown()' on unhandled events, and '${requirementsProvider.javaClass.shortQualifiedName}' fails that"
            }
        }
    }

    override fun verifyFor(
        function: KFunction<*>,
        locallySkipIntentChecks: Boolean,
        locallySkippedIntents: Set<GatewayIntent>,
        eventErasure: Class<*>,
    ): Boolean {
        // Make sure custom events don't implement JDA and BC generic events
        if (eventErasure.isSubclassOf<GenericEvent>()) {
            check(eventErasure.packageName.startsWith("net.dv8tion.jda.api.events")) {
                "Custom events must not implement ${GenericEvent::class.java.name}!"
            }
        } else if (eventErasure.isSubclassOf<BGenericEvent>()) {
            check(eventErasure.packageName.startsWith("io.github.freya022.botcommands.api")) {
                "Custom events must not implement ${BGenericEvent::class.java.name}!"
            }
        }

        return locallySkipIntentChecks || checkIntents(function, eventErasure, locallySkippedIntents)
    }

    private fun checkIntents(function: KFunction<*>, eventErasure: Class<*>, locallySkippedIntents: Set<GatewayIntent>): Boolean {
        if (eventErasure.isSubclassOf<Event>()) {
            @Suppress("UNCHECKED_CAST")
            val requiredIntents = GatewayIntent.fromEvents(eventErasure as Class<out Event>)
            val missingIntents = getMissingIntents(requiredIntents, locallySkippedIntents)
            if (missingIntents.isNotEmpty()) {
                logger.debug { "Skipping JDA event listener ${function.shortSignature} as it is missing intents: $missingIntents" }
                return false
            }

            // Cannot check for RawGatewayEvent as JDA is not present yet and there is no config for it
        } else if (!eventErasure.isSubclassOf<BEvent>()) {
            return checkCustomEventRequirements(function, eventErasure, locallySkippedIntents)
        }

        return true
    }

    private fun checkCustomEventRequirements(function: KFunction<*>, eventErasure: Class<*>, locallySkippedIntents: Set<GatewayIntent>): Boolean {
        check(customEventRequirementsProviders.isNotEmpty()) {
            "No ${classRef<CustomEventRequirementsProvider>()} are available (custom event listener at ${function.shortSignature})"
        }

        val passedRequirementProviders = ArrayList<CustomEventRequirementsProvider>(1)
        for (requirementsProvider in customEventRequirementsProviders) {
            val eventRequirements = requirementsProvider.get(eventErasure)
            if (eventRequirements.areUnknown()) {
                continue
            } else if (eventRequirements.isEmpty()) {
                passedRequirementProviders.add(requirementsProvider)
                continue
            }

            val missingIntents = getMissingIntents(eventRequirements.getIntents(), locallySkippedIntents)
            if (missingIntents.isNotEmpty()) {
                logger.debug { "Skipping custom event listener ${function.shortSignature} as it is missing intents: $missingIntents" }
                return false
            }

            passedRequirementProviders.add(requirementsProvider)
        }

        if (passedRequirementProviders.size > 1) {
            throwState("Multiple ${classRef<CustomEventRequirementsProvider>()} returned requirements for '${eventErasure.shortQualifiedName}':\n${passedRequirementProviders.joinAsList { it.javaClass.shortQualifiedName }}")
        } else if (passedRequirementProviders.isNotEmpty()) {
            return true
        } else {
            throwState("No ${classRef<CustomEventRequirementsProvider>()} returned requirements for '${eventErasure.shortQualifiedName}', available providers:\n${customEventRequirementsProviders.joinAsList { it.javaClass.shortQualifiedName }}")
        }
    }

    private fun getMissingIntents(requiredIntents: Set<GatewayIntent>, locallySkippedIntents: Set<GatewayIntent>): Set<GatewayIntent> {
        return requiredIntents - jdaService.intents - config.ignoredIntents - locallySkippedIntents
    }

    private interface FakeEvent
}
