package dev.freya02.botcommands.jda.ktx

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.test.Test
import kotlin.test.fail

class MigrationSnapshotTest {

    private val kspOutput = Path(System.getenv("KSP_OUTPUT"))

    @Test
    fun `Ensure generated migration files are not modified`() {
        val diffFiles = mutableSetOf<String>()

        MigrationSnapshotTest::class.java.getResourceAsStream("/ksp_snapshot.zip")!!.use { inputStream ->
            ZipInputStream(inputStream).use { zipStream ->
                var entry: ZipEntry? = null
                while (zipStream.nextEntry.also { entry = it } != null) {
                    if (entry!!.isDirectory) continue
                    val frozenBytes = zipStream.readAllBytes()
                    val generatedBytes = kspOutput.resolve(entry.name).readBytes()

                    if (!(frozenBytes contentEquals generatedBytes)) {
                        diffFiles += entry.name
                    }
                }
            }
        }

        if (diffFiles.isNotEmpty()) {
            fail("Migration files should have not changed:\n${diffFiles.joinToString("\n")}")
        }
    }
}
