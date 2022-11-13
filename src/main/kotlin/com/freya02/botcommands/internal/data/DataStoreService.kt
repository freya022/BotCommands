package com.freya02.botcommands.internal.data

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import com.freya02.botcommands.internal.core.db.Database
import com.freya02.botcommands.internal.core.db.Transaction
import com.freya02.botcommands.internal.core.db.isUniqueViolation
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.sql.SQLException
import java.sql.Timestamp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ConditionalService(dependencies = [Database::class])
internal class DataStoreService(
    private val database: Database,
    private val handlerContainer: DataStoreHandlerContainer,
    private val context: BContext,
    private val coroutineScopesConfig: BCoroutineScopesConfig
) {
    private val logger = Logging.getLogger()

    init {
        database.transactional {
            preparedStatement("delete from bc_data where lifetime_type = ?") {
                runBlocking { executeUpdate(LifetimeType.EPHEMERAL.id) }
            }

            preparedStatement("delete from bc_data where expiration_timestamp < now()") {
                runBlocking { executeUpdate(*emptyArray<Any>()) }
            }

            preparedStatement("select id, (extract(epoch from expiration_timestamp - now()) * 1000)::bigint as delayMilliseconds, timeout_handler_id from bc_data") {
                runBlocking {
                    executeQuery(*emptyArray<Any>()).forEach {
                        val dataId: String = it["id"]
                        val delayMilliseconds: Long = it["delayMilliseconds"]

                        scheduleDataTimeout(dataId, delayMilliseconds.milliseconds)
                    }
                }
            }
        }
    }

    suspend fun getData(id: String): DataEntity? = database.transactional {
        preparedStatement("select * from bc_data where id = ? limit 1") {
            val result = executeQuery(*arrayOf(id)).readOnce() ?: return@preparedStatement null

            DataEntity.fromDBResult(result)
        }
    }

    suspend fun putData(entity: PartialDataEntity, postInsert: suspend Transaction.() -> Unit = {}): String {
        for (count in 1 .. 10) {
            try {
                return database.transactional {
                    val id = Utils.randomId(64)

                    preparedStatement(
                        """
                        insert into bc_data (id, data, data_type, lifetime_type, expiration_timestamp, timeout_handler_id)
                        VALUES (?, ?, ?, ?, ?, ?);""".trimIndent()
                    ) {
                        executeUpdate(
                            id,
                            entity.data,
                            entity._dataType,
                            entity.lifetimeType.id,
                            entity.expiration?.let { Timestamp.from(it.expirationInstant.toJavaInstant()) },
                            entity.expiration?.handlerName
                        )
                    }

                    postInsert()

                    return@transactional id
                }.also { dataId ->
                    entity.expiration?.let {
                        scheduleDataTimeout(dataId, entity.expiration.expirationInstant - Clock.System.now())
                    }
                }
            } catch (e: SQLException) {
                when {
                    e.isUniqueViolation() -> if (count == 10) throw e
                    else -> throw e
                }
            }
        }

        throwInternal("Unable to insert in data store after 10 tries, no exceptions have been caught")
    }

    private fun scheduleDataTimeout(dataId: String, delay: Duration) = coroutineScopesConfig.dataTimeoutScope.launch {
        delay(delay)

        when (val data = getData(dataId)) {
            //TODO may be nice to cancel this job if the ID was manually deleted
            null -> logger.trace("Data not found for ID '$dataId'") //Might be normal if it was cleanup up by the user
            else -> data.let { dataEntity ->
                runCatching {
                    database.transactional {
                        preparedStatement("delete from bc_data where id = ?") {
                            executeUpdate(*arrayOf(dataId))
                        }
                    }
                }.onFailure { e ->
                    logger.error("An exception occurred while deleting a data entity, '$dataId'", e)
                    context.dispatchException("An exception occurred while deleting a data entity", e)
                }

                val handlerName = data.expiration?.handlerName ?: let {
                    logger.warn("A timeout was scheduled for a DataEntity with no expiration object")
                    return@launch
                }
                val timeoutHandler = handlerContainer.timeoutHandlers[handlerName] ?: let {
                    logger.warn("No timeout handler found for '${handlerName}'")

                    return@launch
                }

                runCatching {
                    timeoutHandler.execute(dataEntity)
                }.onFailure { e ->
                    logger.error("An exception occurred while running a data entity timeout handler, '$dataId'", e)
                    context.dispatchException("An exception occurred while running a data entity timeout handler", e)
                }
            }
        }
    }

    suspend fun deleteData(componentsIds: List<String>): Int = database.transactional {
        return preparedStatement("delete from bc_data where id = any(?)") {
            executeUpdate(componentsIds.toTypedArray())
        }
    }

    suspend fun deleteReturningData(componentsIds: List<String>): DeletedData = database.transactional {
        return preparedStatement("delete from bc_data where id = any(?) returning *") {
            executeQuery(componentsIds.toTypedArray())
                .map { DataEntity.fromDBResult(it) }
                .let { dataEntities -> DeletedData(dataEntities.size, dataEntities) }
        }
    }

    internal class DeletedData(val rowsAffected: Int, val items: List<DataEntity>)
}