package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.test.services.annotations.RequireProfile
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
@RequireProfile(Profile.DEV)
class MyDevService {
    init {
        logger.trace { "Enabled dev service" }
    }
}