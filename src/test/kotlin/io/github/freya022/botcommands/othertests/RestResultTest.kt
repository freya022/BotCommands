package io.github.freya022.botcommands.othertests

import io.github.freya022.botcommands.api.core.utils.awaitCatching
import io.github.freya022.botcommands.api.core.utils.ignore
import io.github.freya022.botcommands.api.core.utils.recover
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.internal.requests.CompletedRestAction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

object RestResultTest {
    private fun restException(): RestAction<Nothing> {
        return CompletedRestAction(null, ErrorResponseException.create(ErrorResponse.ALREADY_CROSSPOSTED, Response(Exception(), emptySet())))
    }

    @Test
    fun `Recover error response`() = runBlocking {
        val restResult = restException()
            .awaitCatching()
            .recover(ErrorResponse.ALREADY_CROSSPOSTED) {
                Math.PI
            }

        assertNull(restResult.exceptionOrNullIfIgnored(), "Should have been recovered")
        assertNull(restResult.exceptionOrNull(), "Should have been recovered")
        val value = assertDoesNotThrow("Should have been recovered") { restResult.getOrThrow() }
        assertEquals(value, Math.PI)
    }

    @Test
    fun `Ignore error responses`() = runBlocking {
        val restResult = restException()
            .awaitCatching()
            .ignore(ErrorResponse.ALREADY_CROSSPOSTED)

        assertDoesNotThrow("Should not throw as the error response has been ignored") { restResult.orThrow() }
        assertNull(restResult.exceptionOrNullIfIgnored(), "Should be null as it has been ignored")
        assertNotNull(restResult.exceptionOrNull(), "Should not be null as an exception happened, and this ignore exceptions")
        assertNull(restResult.getOrNull(), "Should be null")
    }

    @Test
    fun `Handle error responses`() = runBlocking {
        val restResult = restException()
            .awaitCatching()
            .ignore(ErrorResponse.ALREADY_CROSSPOSTED)

        assertDoesNotThrow("Should not throw as the error response has been handled (ignored)") { restResult.orThrow() }
        assertNull(restResult.exceptionOrNullIfIgnored(), "Should be null as it has been handled (ignored)")
        assertNotNull(restResult.exceptionOrNull(), "Should not be null as an exception happened, and this handles (ignores) exceptions")
        assertNull(restResult.getOrNull(), "Should be null")
    }
}