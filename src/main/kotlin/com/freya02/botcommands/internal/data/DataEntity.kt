package com.freya02.botcommands.internal.data

import com.freya02.botcommands.internal.core.db.DBResult
import kotlinx.datetime.toKotlinInstant
import java.sql.Timestamp

@Deprecated("To be removed")
internal class DataEntity(
    val id: String,
    data: String,
    dataType: String,
    lifetimeType: LifetimeType,
    expiration: DataEntityExpiration?
): PartialDataEntity(data, dataType, lifetimeType, expiration) {
    companion object {
        internal fun fromDBResult(rs: DBResult) = DataEntity(
            rs["id"],
            rs["data"],
            rs["data_type"],
            LifetimeType.fromId(rs["lifetime_type"]),
            rs.get<Timestamp?>("expiration_timestamp")?.let {
                DataEntityExpiration(
                    it.toInstant().toKotlinInstant(),
                    rs["timeout_handler_id"]
                )
            }
        )
    }
}
