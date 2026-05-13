package io.github.freya022.botcommands.commands.application.diff

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.core.utils.readResource
import io.github.freya022.botcommands.internal.commands.application.diff.DiffLoggerImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

object JsonDiffTest {

    private val mapper = ObjectMapper()

    @ParameterizedTest
    @EnumSource(DiffEngine::class)
    fun `Different command description`(diffEngine: DiffEngine) {
        runTest(diffEngine, folderName = "diff_command_description", shouldBeEqual = false)
    }

    @ParameterizedTest
    @EnumSource(DiffEngine::class)
    fun `Different option order`(diffEngine: DiffEngine) {
        runTest(diffEngine, folderName = "diff_option_order", shouldBeEqual = false)
    }

    @ParameterizedTest
    @EnumSource(DiffEngine::class)
    fun `Different command order`(diffEngine: DiffEngine) {
        runTest(diffEngine, folderName = "diff_command_order", shouldBeEqual = true)
    }

    @ParameterizedTest
    @EnumSource(DiffEngine::class)
    fun `Add option in subcommand`(diffEngine: DiffEngine) {
        runTest(diffEngine, folderName = "add_option_in_subcommand", shouldBeEqual = false)
    }

    @Suppress("UNCHECKED_CAST")
    private fun runTest(diffEngine: DiffEngine, folderName: String, shouldBeEqual: Boolean) {
        val oldMap: List<Map<String, *>> = readResource("/commands_data/$folderName/old.json").let(mapper::readValue)
        val newMap: List<Map<String, *>> = readResource("/commands_data/$folderName/new.json").let(mapper::readValue)

        DiffLoggerImpl("tests").apply {
            val isEqual = diffEngine.instance.checkCommands(oldMap, newMap)
            printLogs()
            assertEquals(shouldBeEqual, isEqual)
        }
    }
}
