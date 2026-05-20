package io.github.freya022.botcommands.internal.core.db

import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.oshai.kotlinlogging.KLogger

object DatabaseSchemaHelper {

    /**
     * @param latestVersion \
     *        If the build script has `4.0.0-alpha.1_DEV`, use the next release version, in this case `4.0.0-alpha.2`,
     *        remember to update the `schema_version` row in the migration script too
     * @param featureName \
     *        "schema version for the ... feature"
     * @param fallbackMessage \
     *        "<message>. ..."
     */
    suspend fun validateSchemaVersion(
        logger: KLogger,
        database: Database,
        schemaName: String,
        latestVersion: String,
        featureName: String,
        fallbackMessage: String,
    ): Boolean {
        val currentVersion = try {
            database.preparedStatement("SELECT version FROM ${schemaName}.schema_version", readOnly = true) {
                executeQuery().read().getString(1)
            }
        } catch (e: Exception) {
            // Let's see if there is a 3.X schema
            val oldSchemaVersion = try {
                database.preparedStatement("SELECT version from bc.bc_version", readOnly = true) {
                    executeQuery().read().getString(1)
                }
            } catch (suppressed: Exception) {
                // Likely both schemas dont exist yet
                e.addSuppressed(suppressed)
                // TODO figure out where to document the (initial) schema migration
                logger.warn(e) { "Could not check schema version for the $featureName feature. $fallbackMessage" }
                return false
            }

            // TODO document 3.X -> 4.X migration process
            logger.warn { "The 3.X schema needs to be migrated to 4.X. Current version: '$oldSchemaVersion'. $fallbackMessage" }
            return false
        }

        if (currentVersion != latestVersion) {
            // TODO link to wiki instead of recommending stuff here
            logger.warn { "The current schema version for the $featureName feature is '$currentVersion', but '${latestVersion}' was expected, please upgrade with the help of the migration scripts, do backups if necessary. $fallbackMessage" }
            return false
        }

        return true
    }
}
