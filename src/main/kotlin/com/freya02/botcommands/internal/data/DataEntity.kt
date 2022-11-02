package com.freya02.botcommands.internal.data

import com.freya02.botcommands.internal.core.db.DBResult
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.sql.Timestamp

class DataEntity(
    val id: String,
    data: String,
    lifetimeType: LifetimeType,
    expirationTimestamp: Instant?,
    timeoutHandlerId: String
): PartialDataEntity(data, lifetimeType, expirationTimestamp, timeoutHandlerId) {
    companion object {
        internal fun fromDBResult(rs: DBResult) = DataEntity(
            rs["id"],
            rs["data"],
            LifetimeType.fromId(rs["lifetime_type"]),
            rs.get<Timestamp?>("expiration_timestamp")?.toInstant()?.toKotlinInstant(),
            rs["timeout_handler_id"],
        )
    }
}
