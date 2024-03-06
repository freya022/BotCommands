package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy

// Even if this is lazy, this should throw as Service1 has multiple definitions and no name matches
@Lazy
@BService
class NonUniqueProviderTest(val service1: Service1) {
    class Service1()

    @BConfiguration
    object Service1Provider {
        // Commenting this should NOT throw, as NonUniqueProviderTest is lazy
        @BService
        fun provider1() = Service1()
        // Uncomment to throw
//        @BService
//        fun provider2() = Service1()
    }
}