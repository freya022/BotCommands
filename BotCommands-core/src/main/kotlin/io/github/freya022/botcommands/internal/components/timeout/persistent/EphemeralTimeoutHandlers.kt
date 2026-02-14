package io.github.freya022.botcommands.internal.components.timeout.persistent

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.EphemeralHandlers
import io.github.freya022.botcommands.internal.components.annotations.RequiresPersistentComponents

@BService
@RequiresPersistentComponents // Local components store their handlers in their data directly
internal class EphemeralTimeoutHandlers : EphemeralHandlers<suspend () -> Unit>()
