package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.test_kt.services.annotations.RequiredNumber
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
@RequiredNumber(1)
class MyRNGService {
    init {
        logger.trace { "Should play some lottery today" }
    }
}