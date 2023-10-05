package com.freya02.botcommands.test_kt

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.DefaultMessagesSupplier
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.localization.DefaultDefaultMessagesSupplier

@BService
class MyDefaultMessagesSupplier(context: BContext) : DefaultMessagesSupplier by DefaultDefaultMessagesSupplier(context)