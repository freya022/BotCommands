package io.github.freya022.botcommands.internal.core.db

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService

internal object RequiresDatabaseChecker : CustomConditionChecker<RequiresDatabase> {
    override val annotationType: Class<RequiresDatabase> = RequiresDatabase::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiresDatabase
    ): String? {
        serviceContainer.getService<ConnectionSupplier>() // May throw, will be caught
        return null // Everything's fine
    }
}