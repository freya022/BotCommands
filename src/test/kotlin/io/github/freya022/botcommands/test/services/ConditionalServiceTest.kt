package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService

@BService
@ConditionalService(ConditionalServiceTest.Companion::class)
@RequiresComponents
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

        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? = serviceErrorMessage
    }
}