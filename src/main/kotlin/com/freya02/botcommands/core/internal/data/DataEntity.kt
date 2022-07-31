package com.freya02.botcommands.core.internal.data

import com.freya02.botcommands.core.internal.db.DBResult
import java.sql.Timestamp
import java.time.LocalDateTime

class DataEntity(
    val id: String,
    data: String,
    lifetimeType: LifetimeType,
    expirationTimestamp: LocalDateTime?,
    timeoutHandlerId: String
): PartialDataEntity(data, lifetimeType, expirationTimestamp, timeoutHandlerId) {
    companion object {
        internal fun fromDBResult(rs: DBResult) = DataEntity(
            rs["id"],
            rs["data"],
            LifetimeType.fromId(rs["lifetime_type"]),
            rs.get<Timestamp?>("expiration_timestamp")?.toLocalDateTime(),
            rs["timeout_handler_id"],
        )
    }
}
