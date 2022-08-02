package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.components.ComponentErrorReason

internal class HandleComponentResult(
    val errorReason: ComponentErrorReason?,
    /**
     * Whether additional resources (such as the components handler maps) should get cleaned up
     */
    val shouldDelete: Boolean
)