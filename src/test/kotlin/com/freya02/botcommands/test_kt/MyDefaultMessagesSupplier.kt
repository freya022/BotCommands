package com.freya02.botcommands.test_kt

import com.freya02.botcommands.api.core.DefaultMessagesSupplier
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.internal.DefaultDefaultMessagesSupplier

@BService
@ServiceType([DefaultMessagesSupplier::class])
object MyDefaultMessagesSupplier : DefaultMessagesSupplier by DefaultDefaultMessagesSupplier