package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.registerServiceSupplier
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.DefaultPermissionLocalization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.PermissionLocalization
import io.github.freya022.botcommands.framework.utils.createTest
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.Permission
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

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

    @Test
    fun `Can override autoconfiguration`() {
        val expected = mockk<PermissionLocalization>()
        val context = BotCommands.createTest {
            services {
                registerServiceSupplier<PermissionLocalization> { expected }
            }
        }

        val actual = assertDoesNotThrow { context.getService<PermissionLocalization>() }
        assertSame(expected, actual)
    }
}