package io.github.freya022.botcommands.localization

import io.github.freya022.botcommands.api.localization.DefaultPermissionLocalization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PermissionLocalizationTests {

    @Test
    fun `Should use Permission's enum name when there are no bundles`() {
        val localizationService = mockk<LocalizationService> {
            every { getInstance(any(), any()) } returns null
        }

        val permissionLocalization = DefaultPermissionLocalization(localizationService)
        val actual = permissionLocalization.localize(Permission.VIEW_CHANNEL, Locale.FRENCH)

        assertEquals("View Channels", actual)
    }

    @Test
    fun `Should be able to use the default bundle`() {
        val expected = "Localized permission name"
        val localizationService = mockk<LocalizationService> {
            every { getInstance("Permissions", Locale.FRENCH)?.get("VIEW_CHANNEL")?.localize() } returns expected
        }

        val permissionLocalization = DefaultPermissionLocalization(localizationService)
        val actual = permissionLocalization.localize(Permission.VIEW_CHANNEL, Locale.FRENCH)

        assertEquals(expected, actual)
    }
}
