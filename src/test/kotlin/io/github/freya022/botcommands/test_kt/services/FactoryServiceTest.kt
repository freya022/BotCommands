package io.github.freya022.botcommands.test_kt.services

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies

//Can test failure if FactoryServiceTest is not instantiable, by commenting @Dependencies
@BService
@Dependencies(FactoryServiceTest::class)
class FactoryServiceTestUser(service: FactoryServiceTest)

class FactoryServiceTest private constructor() {
//    @BService //Not necessary
    object FactoryServiceTestProvider {
        @BService
        @ConditionalService(ConditionalServiceTest.Companion::class)
        fun getFactory() = FactoryServiceTest()
    }
}

@BService
class PropertyFactoryServiceTest private constructor() {
    //    @BService //Not necessary
    object PropertyFactoryServiceTestProvider {
        @get:BService
        val factory = PropertyFactoryServiceTest()
    }
}
