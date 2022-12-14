package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.core.annotations.BService

@BService
internal class EphemeralTimeoutHandlers : EphemeralHandlers<suspend () -> Unit>()