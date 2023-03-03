package com.freya02.botcommands.test

import com.freya02.botcommands.internal.core.Version
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VersionTest {
    @Test
    fun `parse invalid`() {
        assertThrows<IllegalArgumentException> { Version.get("") }
        assertThrows<IllegalArgumentException> { Version.get("3.8") }
        assertThrows<IllegalArgumentException> { Version.get("3.8.0-ok.3") }
    }

    @Test
    fun `parse valid`() {
        Version.get("3.8.0").also {
            assertEquals(3, it.major)
            assertEquals(8, it.minor)
            assertEquals(0, it.revision)
            assertNull(it.classifier)
        }

        Version.get("2.9.1-beta.2").also {
            assertEquals(2, it.major)
            assertEquals(9, it.minor)
            assertEquals(1, it.revision)

            assertNotNull(it.classifier)
            assertEquals("beta", it.classifier!!.name)
            assertEquals(2, it.classifier.version)
        }
    }

    @Test
    fun `compare versions`() {
        assert(Version.get("3.8.0") > Version.get("2.9.1-beta.2"))
        assert(Version.get("3.8.0") > Version.get("3.8.0-beta.2"))
        assert(Version.get("3.8.1") > Version.get("3.8.0"))
        assert(Version.get("3.9.0") > Version.get("3.8.0"))
        assert(Version.get("4.8.0") > Version.get("3.8.0"))
        assert(Version.get("3.8.0-beta.3") > Version.get("3.8.0-beta.2"))
        assert(Version.get("3.8.0-beta.2") > Version.get("3.8.0-alpha.3"))
        assert(Version.get("3.9.0-alpha.2") > Version.get("3.8.0-alpha.3"))
        assert(Version.get("3.0.0-alpha.5") > Version.get("3.0.0-alpha.4"))
        assert(Version.get("3.0.0-beta.5") > Version.get("3.0.0-alpha.6"))
    }
}