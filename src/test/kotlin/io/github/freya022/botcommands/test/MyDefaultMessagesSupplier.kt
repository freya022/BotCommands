package io.github.freya022.botcommands.test

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DefaultMessagesSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.localization.DefaultDefaultMessagesSupplier

@BService
class MyDefaultMessagesSupplier(context: BContext) : DefaultMessagesSupplier by DefaultDefaultMessagesSupplier(context)