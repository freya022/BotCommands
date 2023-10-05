package io.github.freya022.botcommands.test_kt.services

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies

@BService
@Dependencies(Components::class)
@ConditionalService(ConditionalServiceTest.Companion::class)
class ConditionalServiceTest {
    @BService
    class ConditionalServiceTester(serviceContainer: ServiceContainer) {
        init {
            val serviceError = serviceContainer.tryGetService(ConditionalServiceTest::class).serviceError
            if (serviceError != null) {
                println("Could not get ${ConditionalServiceTest::class.simpleName}: ${serviceError.errorMessage}")
            } else {
                println("${ConditionalServiceTest::class.simpleName} passed")
            }
        }
    }

    companion object : ConditionalServiceChecker {
        //Compute the "luck" only once, else it's going to mess up the service creation
        private val serviceErrorMessage = when {
            Math.random() > 0.5 -> null
            else -> "Bad luck"
        }

        override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>): String? = serviceErrorMessage
    }
}