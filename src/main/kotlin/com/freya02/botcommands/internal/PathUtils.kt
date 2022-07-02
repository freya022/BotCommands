package com.freya02.botcommands.internal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

fun Path.overwriteBytes(bytes: ByteArray): Path =
    Files.write(this, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)