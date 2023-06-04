package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import com.freya02.botcommands.api.core.service.annotations.Dependencies

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
