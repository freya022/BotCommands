package io.github.freya022.botcommands.internal.commands.application.diff

import net.dv8tion.jda.api.interactions.commands.OptionType

private typealias Command = Map<String, *>
private typealias Option = Map<String, *>
private typealias Choice = Map<String, *>
private typealias Object = Map<String, *>
private typealias ObjectName = String

internal object NewApplicationCommandDiffEngine : ApplicationCommandDiffEngine {
    context(DiffLogger)
    override fun checkCommands(oldCommands: List<Command>, newCommands: List<Command>): Boolean {
        val addedCommands = newCommands.toNames() - oldCommands.toNames()
        if (addedCommands.isNotEmpty()) log { "Added top-level commands: ${addedCommands.joinToString()}" }

        val removedCommands = oldCommands.toNames() - newCommands.toNames()
        if (removedCommands.isNotEmpty()) log { "Removed top-level commands: ${removedCommands.joinToString()}" }

        val isSame = forEachByName(oldCommands, newCommands) { commandName, oldCommand, newCommand ->
            checkProperties(oldCommand, newCommand, "Top-level command '$commandName'") &&
                    checkOptions(commandName, oldCommand, newCommand) &&
                    checkSubcommands(commandName, oldCommand, newCommand) &&
                    checkSubcommandGroups(commandName, oldCommand, newCommand)
        }

        return addedCommands.isEmpty() && removedCommands.isEmpty() && isSame
    }

    context(DiffLogger)
    private fun checkSubcommandGroups(
        topLevelName: ObjectName,
        oldCommand: Command,
        newCommand: Command,
    ): Boolean {
        val oldSubcommandGroups = oldCommand.subcommandGroups
        val newSubcommandGroups = newCommand.subcommandGroups

        val addedSubcommandGroups = newSubcommandGroups.toNames() - oldSubcommandGroups.toNames()
        if (addedSubcommandGroups.isNotEmpty()) log { "Added subcommand groups to '$topLevelName': ${addedSubcommandGroups.joinToString()}" }

        val removedSubcommandGroups = oldSubcommandGroups.toNames() - newSubcommandGroups.toNames()
        if (removedSubcommandGroups.isNotEmpty()) log { "Removed subcommand groups from '$topLevelName': ${removedSubcommandGroups.joinToString()}" }

        val isSame = forEachByName(oldSubcommandGroups, newSubcommandGroups) { subcommandGroupName, oldSubcommandGroup, newSubcommandGroup ->
            checkProperties(oldSubcommandGroup, newSubcommandGroup, "Subcommand group '$topLevelName $subcommandGroupName'") &&
                    checkSubcommands("$topLevelName $subcommandGroupName", oldSubcommandGroup, newSubcommandGroup)
        }

        return addedSubcommandGroups.isEmpty() && removedSubcommandGroups.isEmpty() && isSame
    }

    context(DiffLogger)
    private fun checkSubcommands(
        parentName: ObjectName,
        oldCommand: Command,
        newCommand: Command,
    ): Boolean {
        val oldSubcommands = oldCommand.subcommands
        val newSubcommands = newCommand.subcommands

        val addedSubcommands = newSubcommands.toNames() - oldSubcommands.toNames()
        if (addedSubcommands.isNotEmpty()) log { "Added subcommands to '$parentName': ${addedSubcommands.joinToString()}" }

        val removedSubcommands = oldSubcommands.toNames() - newSubcommands.toNames()
        if (removedSubcommands.isNotEmpty()) log { "Removed subcommands from '$parentName': ${removedSubcommands.joinToString()}" }

        val isSame = forEachByName(oldSubcommands, newSubcommands) { subcommandName, oldSubcommand, newSubcommand ->
            checkProperties(oldSubcommand, newSubcommand, "Subcommand '$parentName $subcommandName'") &&
                    checkOptions("Subcommand '$parentName $subcommandName'", oldSubcommand, newSubcommand)
        }

        return addedSubcommands.isEmpty() && removedSubcommands.isEmpty() && isSame
    }

    context(DiffLogger)
    private fun checkOptions(cmdName: ObjectName, oldCommand: Command, newCommand: Command): Boolean {
        val oldOptions = oldCommand.options
        val newOptions = newCommand.options

        val addedOptions = newOptions.toNames() - oldOptions.toNames()
        if (addedOptions.isNotEmpty()) log { "Added options to '$cmdName': ${addedOptions.joinToString()}" }

        val removedOptions = oldOptions.toNames() - newOptions.toNames()
        if (removedOptions.isNotEmpty()) log { "Removed options from '$cmdName': ${removedOptions.joinToString()}" }

        val isSame = forEachOption(oldOptions, newOptions) { optionName, oldOptionIndex, oldOption, newOptionIndex, newOption ->
            var isSame = true
            if (oldOptionIndex != newOptionIndex) {
                log { "Option '$optionName' from '$cmdName' moved from #$oldOptionIndex to #$newOptionIndex" }
                isSame = false
            }

            isSame &&
                    checkProperties(oldOption, newOption, "Option '$optionName' in '$cmdName'") &&
                    checkChoices(cmdName, optionName, oldOption, newOption)
        }

        return addedOptions.isEmpty() && removedOptions.isEmpty() && isSame
    }

    context(DiffLogger)
    private fun checkChoices(cmdName: ObjectName, optionName: ObjectName, oldOption: Option, newOption: Option): Boolean {
        val oldChoices = oldOption.choices
        val newChoices = newOption.choices

        val addedChoices = newChoices.toNames() - oldChoices.toNames()
        if (addedChoices.isNotEmpty()) log { "Added choices to option '$optionName' of '$cmdName': ${addedChoices.joinToString()}" }

        val removedChoices = oldChoices.toNames() - newChoices.toNames()
        if (removedChoices.isNotEmpty()) log { "Removed choices from option '$optionName' of '$cmdName': ${removedChoices.joinToString()}" }

        val isUnmodified = forEachByName(oldChoices, newChoices) { choiceName, oldChoice, newChoice ->
            checkProperties(oldChoice, newChoice, "Choice '$choiceName' in option '$optionName' of '$cmdName'")
        }

        return addedChoices.isEmpty() && removedChoices.isEmpty() && isUnmodified
    }

    context(DiffLogger)
    private fun checkProperties(oldCommand: Command, newCommand: Command, cmdName: ObjectName): Boolean {
        val oldPropertyNames = oldCommand.keys
        val newPropertyNames = newCommand.keys

        val addedPropertyNames = newPropertyNames - oldPropertyNames
        if (addedPropertyNames.isNotEmpty()) log { "Added properties to $cmdName: ${addedPropertyNames.joinToString()}" }

        val removedPropertyNames = oldPropertyNames - newPropertyNames
        if (removedPropertyNames.isNotEmpty()) log { "Removed properties from $cmdName: ${removedPropertyNames.joinToString()}" }

        var hasModifications = false
        // Don't check order-sensitive properties, they're done manually
        val allProperties = (oldPropertyNames + newPropertyNames) - "options" - "choices"
        allProperties.forEach { propertyName ->
            val oldProperty = oldCommand[propertyName]
            val newProperty = newCommand[propertyName]
            if (oldProperty?.javaClass != newProperty?.javaClass || oldProperty != newProperty) {
                fun Any?.q() = if (this == null) "null" else "'$this'"
                log { "Property '$propertyName' from $cmdName changed from ${oldProperty.q()} -> ${newProperty.q()}" }
                hasModifications = true
            }
        }
        return !hasModifications
    }

    private fun List<Object>.toNames(): Set<ObjectName> = mapTo(hashSetOf()) { it["name"] as ObjectName }

    private fun <T : Object> forEachByName(old: List<T>, new: List<T>, block: (name: ObjectName, old: T, new: T) -> Boolean): Boolean {
        var isSame = true
        old.toNames().intersect(new.toNames()).forEach { objectName ->
            val oldObject = old.first { it["name"] == objectName }
            val newObject = new.first { it["name"] == objectName }
            isSame = isSame && block(objectName, oldObject, newObject)
        }
        return isSame
    }

    private fun forEachOption(old: List<Option>, new: List<Option>, block: (name: String, oldIndex: Int, old: Option, newIndex: Int, new: Option) -> Boolean): Boolean {
        var isSame = true
        new.toNames().intersect(old.toNames()).forEach { optionName ->
            val oldOptionIndex = old.indexOfFirst { it["name"] == optionName }
            val newOptionIndex = new.indexOfFirst { it["name"] == optionName }
            val oldOption = old[oldOptionIndex]
            val newOption = new[newOptionIndex]
            isSame = isSame && block(optionName, oldOptionIndex, oldOption, newOptionIndex, newOption)
        }
        return isSame
    }

    @Suppress("UNCHECKED_CAST")
    private val Command.subcommands: List<Command>
        get() {
            val options = this["options"] as List<Command>?
            return options?.filter { it["type"] == OptionType.SUB_COMMAND.key }
                ?: emptyList()
        }

    @Suppress("UNCHECKED_CAST")
    private val Command.subcommandGroups: List<Command>
        get() {
            val options = this["options"] as List<Command>?
            return options?.filter { it["type"] == OptionType.SUB_COMMAND_GROUP.key }
                ?: emptyList()
        }

    @Suppress("UNCHECKED_CAST")
    private val Command.options: List<Option>
        get() {
            val options = this["options"] as List<Option>?
            return options?.filter { it["type"] !in arrayOf(OptionType.SUB_COMMAND_GROUP.key, OptionType.SUB_COMMAND.key) }
                ?: emptyList()
        }

    @Suppress("UNCHECKED_CAST")
    private val Option.choices: List<Choice>
        get() = this["choices"] as List<Choice>? ?: emptyList()
}