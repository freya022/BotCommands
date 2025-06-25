package io.github.freya022.botcommands.framework

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.freya022.botcommands.api.core.db.DBResult
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class DBResultTests {

    @Test
    fun `Use iterator`() {
        val dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
            maximumPoolSize = 1
        })

        dataSource.connection.use { connection ->
            connection.prepareStatement("CREATE TABLE test(data int)").use { statement -> statement.executeUpdate() }

            val expected = 4646923
            connection.prepareStatement("INSERT INTO test (data) VALUES (?)").use { statement ->
                statement.setInt(1, 4646923)
                statement.executeUpdate()
            }

            connection.prepareStatement("SELECT data FROM test").use { statement ->
                val result = DBResult(statement.executeQuery())
                val iterator = result.iterator()
                val nextRow = assertDoesNotThrow { iterator.next() }
                assertEquals(expected, nextRow.getInt(1))
            }
        }
    }
}
