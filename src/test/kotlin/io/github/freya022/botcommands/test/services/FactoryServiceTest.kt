package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies

//Can test failure if FactoryServiceTest is not instantiable, by commenting @Dependencies
@BService
@Dependencies(FactoryServiceTest::class)
class FactoryServiceTestUser(service: FactoryServiceTest)

class FactoryServiceTest private constructor() {
    @BConfiguration
    object FactoryServiceTestProvider {
        @BService
        @ConditionalService(ConditionalServiceTest.Companion::class)
        fun getFactory() = FactoryServiceTest()
    }
}

class PropertyFactoryServiceTest private constructor() {
    @BConfiguration
    object PropertyFactoryServiceTestProvider {
        @get:BService
        val factory = PropertyFactoryServiceTest()
    }
}
