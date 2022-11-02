package com.freya02.botcommands.internal.components.sql

import com.freya02.botcommands.api.components.ComponentType
import com.freya02.botcommands.api.components.FetchedComponent
import com.freya02.botcommands.internal.core.db.DBResult
import com.freya02.botcommands.internal.throwInternal

internal class SQLFetchedComponent(val resultSet: DBResult) : FetchedComponent {
    private val type: ComponentType

    init {
        val typeRaw: Int = resultSet["type"]
        type = ComponentType.fromKey(typeRaw) ?: throwInternal("Couldn't get type for $typeRaw")
    }

    override fun getType(): ComponentType {
        return type
    }
}