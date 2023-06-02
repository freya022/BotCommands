package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.core.service.ConditionalServiceChecker
import com.freya02.botcommands.api.core.service.ServiceContainer
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import com.freya02.botcommands.api.core.service.annotations.Dependencies

@BService
@Dependencies([Components::class])
@ConditionalService([ConditionalServiceTest.Companion::class])
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
        override fun checkServiceAvailability(context: BContext): String? = when {
            Math.random() > 0.5 -> null
            else -> "Bad luck"
        }
    }
}