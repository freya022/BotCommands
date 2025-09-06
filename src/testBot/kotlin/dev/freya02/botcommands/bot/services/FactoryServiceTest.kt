package dev.freya02.botcommands.bot.services

import io.github.freya022.botcommands.api.core.service.annotations.*

//Can test failure if FactoryServiceTest is not instantiable, by commenting @Dependencies
@BService
@Dependencies(FactoryServiceTest::class)
@RequiresDefaultInjection
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
