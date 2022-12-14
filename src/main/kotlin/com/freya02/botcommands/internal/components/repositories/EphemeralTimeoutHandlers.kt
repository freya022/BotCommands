package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.core.annotations.BService

@BService
internal class EphemeralTimeoutHandlers : EphemeralHandlers<suspend () -> Unit>()