package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.test_kt.services.annotations.RequireProfile
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
@RequireProfile(Profile.DEV)
class MyDevService {
    init {
        logger.trace { "Enabled dev service" }
    }
}