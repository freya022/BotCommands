package io.github.freya022.botcommands.internal.commands.application.diff

import io.github.freya022.botcommands.internal.application.diff.*

internal object OldRefactoredApplicationCommandDiffEngine : ApplicationCommandDiffEngine {
    context(DiffLogger)
    override fun checkCommands(oldCommands: List<Map<String, *>>, newCommands: List<Map<String, *>>): Boolean {
        return checkDiff(oldCommands, newCommands, indent = 0)
    }

    context(DiffLogger)
    private fun checkDiff(oldObj: Any?, newObj: Any?, indent: Int): Boolean {
        if (oldObj == null && newObj == null) {
            return logSame(indent) { "Both null" }
        }

        if (oldObj == null) {
            return logDifferent(indent) { "oldObj is null" }
        } else if (newObj == null) {
            return logDifferent(indent) { "newObj is null" }
        }

        if (oldObj.javaClass != newObj.javaClass) {
            return logDifferent(indent) { "Class type not equal: ${oldObj.javaClass.simpleName} to ${newObj.javaClass.simpleName}" }
        }

        return if (oldObj is Map<*, *> && newObj is Map<*, *>) {
            checkMap(oldObj, newObj, indent)
        } else if (oldObj is List<*> && newObj is List<*>) {
            checkList(oldObj, newObj, indent)
        } else {
            return when (oldObj == newObj) {
                true -> logSame(indent) { "Same object: $oldObj" }
                false -> logDifferent(indent) { "Not same object: $oldObj to $newObj" }
            }
        }
    }

    context(DiffLogger)
    private fun checkList(oldList: List<*>, newList: List<*>, indent: Int): Boolean {
        if (oldList.size != newList.size)
            return logDifferent(indent) { "List is not of the same size" }

        oldList.indices.forEach { i ->
            withKey(i.toString()) {
                // Try to find an object with the same content but at a different index
                // Whether it is effectively different depends on the type of the object
                val index = newList.indexOfFirst { ignoreLogs { checkDiff(oldList[i], it, indent + 1) } }
                if (index == -1)
                    return logDifferent(indent) { "List item not found: ${oldList[i]} to ${newList[i]}" }

                if (index != i) {
                    // Found the obj somewhere else, let's see if the object is an option
                    val oldObj = oldList[index]
                    if (oldObj is Map<*, *> && "autocomplete" in oldObj) {
                        // Is an option
                        return logDifferent(indent) { "Final command option has changed place from index $i to $index : ${oldList[i]}" }
                    } else {
                        // Is not an option
                        logSame(indent) { "Found exact object at index $index (original object at $i) : ${oldList[i]}" }
                        return@forEach // Look at other options
                    }
                } else {
                    // Same index
                    logSame(indent) { "Found exact object at same index $i : ${oldList[i]}" }
                    return@forEach // Look at other options
                }
            }
        }

        return true
    }

    context(DiffLogger)
    private fun checkMap(oldMap: Map<*, *>, newMap: Map<*, *>, indent: Int): Boolean {
        val missingKeys = oldMap.keys - newMap.keys
        if (missingKeys.isNotEmpty())
            return logDifferent(indent) { "Missing keys: $missingKeys" }

        val addedKeys = newMap.keys - oldMap.keys
        if (addedKeys.isNotEmpty())
            return logDifferent(indent) { "Added keys: $addedKeys" }

        for (key in oldMap.keys) {
            withKey(key.toString()) {
                if (!checkDiff(oldMap[key], newMap[key], indent + 1)) {
                    return logDifferent(indent) { "Map value not equal for key '$key': ${oldMap[key]} to ${newMap[key]}" }
                }
            }
        }

        return true
    }
}