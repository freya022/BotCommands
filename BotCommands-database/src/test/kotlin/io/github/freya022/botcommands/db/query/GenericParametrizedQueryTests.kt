package io.github.freya022.botcommands.db.query

import io.github.freya022.botcommands.internal.core.db.query.GenericParametrizedQueryFactory.GenericParametrizedQuery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.sql.PreparedStatement
import kotlin.test.assertEquals

object GenericParametrizedQueryTests {

    @MethodSource("parametrizedQueries")
    @ParameterizedTest
    fun `Format parametrized queries`(query: String, expected: String, values: Map<Int, Any>) {
        val statement = mockk<PreparedStatement> {
            every { enquoteLiteral(any()) } answers { callOriginal() }
        }
        val query = GenericParametrizedQuery(statement, query)
        values.forEach { (key, value) -> query.addValue(key, value) }

        assertEquals(expected, query.toSql())
    }

    @JvmStatic
    fun parametrizedQueries(): List<Arguments> = listOf(
        parametrizedQuery(
            name = "Ensure '?' in content is not replaced",
            query = "insert into test (a, b) values (?, ?)",
            expected = "insert into test (a, b) values ('?', 1)",
            values = mapOf(
                1 to "?",
                2 to 1,
            )
        ),
    )

    private fun parametrizedQuery(name: String, query: String, expected: String, values: Map<Int, Any>): Arguments =
        Arguments.argumentSet(name, query, expected, values)
}
