package io.github.freya022.botcommands.othertests

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import io.github.freya022.botcommands.api.core.utils.readResource
import io.github.freya022.botcommands.internal.application.diff.DiffLogger
import io.github.freya022.botcommands.internal.application.diff.DiffLoggerImpl
import io.github.freya022.botcommands.internal.commands.application.diff.NewApplicationCommandDiff
import io.github.freya022.botcommands.test.config.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

object JsonDiffTest {
    @JvmStatic
    @BeforeAll
    fun setup() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        (LoggerFactory.getLogger(DiffLogger::class.java) as Logger).level = Level.TRACE
    }

    @Test
    fun `Different command description`() {
        runTest(folderName = "diff_command_description", shouldBeEqual = false)
    }

    @Test
    fun `Different option order`() {
        runTest(folderName = "diff_option_order", shouldBeEqual = false)
    }

    @Test
    fun `Different command order`() {
        runTest(folderName = "diff_command_order", shouldBeEqual = true)
    }

    @Suppress("UNCHECKED_CAST")
    private fun runTest(folderName: String, shouldBeEqual: Boolean) {
        val oldMap = readResource("/commands_data/$folderName/old.json").let(DefaultObjectMapper::readList) as List<Map<String, *>>
        val newMap = readResource("/commands_data/$folderName/new.json").let(DefaultObjectMapper::readList) as List<Map<String, *>>

        DiffLoggerImpl().apply {
            val isEqual = NewApplicationCommandDiff.checkCommands(oldMap, newMap)
            printLogs()
            assertEquals(shouldBeEqual, isEqual)
        }
    }
}