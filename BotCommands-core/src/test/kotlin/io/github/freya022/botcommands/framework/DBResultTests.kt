package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.api.core.db.DBResult
import io.github.freya022.botcommands.framework.db.TestH2Source
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class DBResultTests {

    @Test
    fun `Use iterator`() {
        val source = TestH2Source()

        source.getConnection().use { connection ->
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
