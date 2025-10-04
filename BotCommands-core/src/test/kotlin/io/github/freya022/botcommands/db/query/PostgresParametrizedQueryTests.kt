package io.github.freya022.botcommands.db.query

import io.github.freya022.botcommands.internal.core.db.query.PostgresParametrizedQueryFactory.PostgresParametrizedQuery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.sql.PreparedStatement
import kotlin.test.assertEquals

object PostgresParametrizedQueryTests {

    @MethodSource("parametrizedQueries")
    @ParameterizedTest
    fun `Format parametrized queries`(query: String, postgresParametrizedQuery: String, expected: String, values: Map<Int, Any>) {
        val statement = mockk<PreparedStatement> {
            every { enquoteLiteral(any()) } answers { callOriginal() }
            every { this@mockk.toString() } returns postgresParametrizedQuery
        }
        val query = PostgresParametrizedQuery(statement, query)
        values.forEach { (key, value) -> query.addValue(key, value) }

        assertEquals(expected, query.toSql())
    }

    @JvmStatic
    fun parametrizedQueries(): List<Arguments> = listOf(
        parametrizedQuery(
            name = "Simple query, with query parameters",
            query = "select component_id from bc_component where lifetime_type = ?",
            postgresParametrizedQuery = "select component_id from bc_component where lifetime_type = ?",
            expected = "select component_id from bc_component where lifetime_type = 1",
            values = mapOf(1 to 1)
        ),
        parametrizedQuery(
            name = "Simple query, replaced by pgjdbc",
            query = "select component_id from bc_component where lifetime_type = ?",
            postgresParametrizedQuery = "select component_id from bc_component where lifetime_type = ('1'::int4)",
            expected = "select component_id from bc_component where lifetime_type = ('1'::int4)",
            values = mapOf(1 to 1)
        ),
        parametrizedQuery(
            name = "All query parameters",
            query = """
                select *
                from (select full_identifier,
                             coalesce(human_identifier, classname)       as human_identifier,
                             coalesce(human_class_identifier, classname) as human_class_identifier,
                             return_type,
                             similarity(classname, ?)                    as overall_similarity,
                             type
                      from doc_view
                               natural left join doc
                      where source_id = ?
                        and type = any (?)
                        and full_identifier % ? -- Uses a fake threshold of 0.1, set above
                     ) as low_accuracy_search
                where overall_similarity > 0.22     --Real threshold
                  and not full_identifier = any (?) --Remove previous results
                order by case when not ? like '%#%' then type end, --Don't order by type if the query asks for identifiers of a class 
                         overall_similarity desc nulls last,
                         full_identifier                           --Class > Method > Field, then similarity
                limit ?;
            """.trimIndent(),
            postgresParametrizedQuery = """
                select *
                from (select full_identifier,
                             coalesce(human_identifier, classname)       as human_identifier,
                             coalesce(human_class_identifier, classname) as human_class_identifier,
                             return_type,
                             similarity(classname, ?)                    as overall_similarity,
                             type
                      from doc_view
                               natural left join doc
                      where source_id = ?
                        and type = any (?)
                        and full_identifier % ? -- Uses a fake threshold of 0.1, set above
                     ) as low_accuracy_search
                where overall_similarity > 0.22     --Real threshold
                  and not full_identifier = any (?) --Remove previous results
                order by case when not ? like '%#%' then type end, --Don't order by type if the query asks for identifiers of a class 
                         overall_similarity desc nulls last,
                         full_identifier                           --Class > Method > Field, then similarity
                limit ?;
            """.trimIndent(),
            expected = "select * from (select full_identifier, coalesce(human_identifier, classname)       as human_identifier, coalesce(human_class_identifier, classname) as human_class_identifier, return_type, similarity(classname, 'hook')                    as overall_similarity, type from doc_view natural left join doc where source_id = 1 and type = any (ARRAY[2, 3]) and full_identifier % 'hook#callback' ) as low_accuracy_search where overall_similarity > 0.22 and not full_identifier = any (ARRAY[]) order by case when not 'hook#callback' like '%#%' then type end, overall_similarity desc nulls last, full_identifier limit 25;",
            values = mapOf(
                1 to "hook",
                2 to 1,
                3 to intArrayOf(2, 3),
                4 to "hook#callback",
                5 to arrayOf<String>(),
                6 to "hook#callback",
                7 to 25,
            )
        ),
        parametrizedQuery(
            name = "Half query parameters, half replaced by pgjdbc",
            query = """
                select *
                from (select full_identifier,
                             coalesce(human_identifier, classname)       as human_identifier,
                             coalesce(human_class_identifier, classname) as human_class_identifier,
                             return_type,
                             similarity(classname, ?)                    as overall_similarity,
                             type
                      from doc_view
                               natural left join doc
                      where source_id = ?
                        and type = any (?)
                        and full_identifier % ? -- Uses a fake threshold of 0.1, set above
                     ) as low_accuracy_search
                where overall_similarity > 0.22     --Real threshold
                  and not full_identifier = any (?) --Remove previous results
                order by case when not ? like '%#%' then type end, --Don't order by type if the query asks for identifiers of a class 
                         overall_similarity desc nulls last,
                         full_identifier                           --Class > Method > Field, then similarity
                limit ?;
            """.trimIndent(),
            postgresParametrizedQuery = """
                select *
                from (select full_identifier,
                             coalesce(human_identifier, classname)       as human_identifier,
                             coalesce(human_class_identifier, classname) as human_class_identifier,
                             return_type,
                             similarity(classname, 'hook')                    as overall_similarity,
                             type
                      from doc_view
                               natural left join doc
                      where source_id = ?
                        and type = any (?)
                        and full_identifier % ? -- Uses a fake threshold of 0.1, set above
                     ) as low_accuracy_search
                where overall_similarity > 0.22     --Real threshold
                  and not full_identifier = any (ARRAY[]) --Remove previous results
                order by case when not ? like '%#%' then type end, --Don't order by type if the query asks for identifiers of a class 
                         overall_similarity desc nulls last,
                         full_identifier                           --Class > Method > Field, then similarity
                limit ?;
            """.trimIndent(),
            expected = "select * from (select full_identifier, coalesce(human_identifier, classname)       as human_identifier, coalesce(human_class_identifier, classname) as human_class_identifier, return_type, similarity(classname, 'hook')                    as overall_similarity, type from doc_view natural left join doc where source_id = 1 and type = any (ARRAY[2, 3]) and full_identifier % 'hook#callback' ) as low_accuracy_search where overall_similarity > 0.22 and not full_identifier = any (ARRAY[]) order by case when not 'hook#callback' like '%#%' then type end, overall_similarity desc nulls last, full_identifier limit 25;",
            values = mapOf(
                1 to "hook",
                2 to 1,
                3 to intArrayOf(2, 3),
                4 to "hook#callback",
                5 to arrayOf<String>(),
                6 to "hook#callback",
                7 to 25,
            )
        ),
        parametrizedQuery(
            name = "All replaced by pgjdbc",
            query = """
                select *
                from (select full_identifier,
                             coalesce(human_identifier, classname)       as human_identifier,
                             coalesce(human_class_identifier, classname) as human_class_identifier,
                             return_type,
                             similarity(classname, ?)                    as overall_similarity,
                             type
                      from doc_view
                               natural left join doc
                      where source_id = ?
                        and type = any (?)
                        and full_identifier % ? -- Uses a fake threshold of 0.1, set above
                     ) as low_accuracy_search
                where overall_similarity > 0.22     --Real threshold
                  and not full_identifier = any (?) --Remove previous results
                order by case when not ? like '%#%' then type end, --Don't order by type if the query asks for identifiers of a class 
                         overall_similarity desc nulls last,
                         full_identifier                           --Class > Method > Field, then similarity
                limit ?;
            """.trimIndent(),
            postgresParametrizedQuery = """
                select *
                from (select full_identifier,
                             coalesce(human_identifier, classname)       as human_identifier,
                             coalesce(human_class_identifier, classname) as human_class_identifier,
                             return_type,
                             similarity(classname, 'hook')                    as overall_similarity,
                             type
                      from doc_view
                               natural left join doc
                      where source_id = 1
                        and type = any (ARRAY[2, 3])
                        and full_identifier % 'hook#callback' -- Uses a fake threshold of 0.1, set above
                     ) as low_accuracy_search
                where overall_similarity > 0.22     --Real threshold
                  and not full_identifier = any (ARRAY[]) --Remove previous results
                order by case when not 'hook#callback' like '%#%' then type end, --Don't order by type if the query asks for identifiers of a class 
                         overall_similarity desc nulls last,
                         full_identifier                           --Class > Method > Field, then similarity
                limit 25;
            """.trimIndent(),
            expected = "select * from (select full_identifier, coalesce(human_identifier, classname)       as human_identifier, coalesce(human_class_identifier, classname) as human_class_identifier, return_type, similarity(classname, 'hook')                    as overall_similarity, type from doc_view natural left join doc where source_id = 1 and type = any (ARRAY[2, 3]) and full_identifier % 'hook#callback' ) as low_accuracy_search where overall_similarity > 0.22 and not full_identifier = any (ARRAY[]) order by case when not 'hook#callback' like '%#%' then type end, overall_similarity desc nulls last, full_identifier limit 25;",
            values = mapOf(
                1 to "hook",
                2 to 1,
                3 to intArrayOf(2, 3),
                4 to "hook#callback",
                5 to arrayOf<String>(),
                6 to "hook#callback",
                7 to 25,
            )
        ),
    )

    private fun parametrizedQuery(name: String, query: String, postgresParametrizedQuery: String, expected: String, values: Map<Int, Any>): Arguments =
        Arguments.argumentSet(name, query, postgresParametrizedQuery, expected, values)
}
