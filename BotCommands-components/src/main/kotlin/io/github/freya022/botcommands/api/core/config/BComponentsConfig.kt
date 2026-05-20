package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL

/**
 * Configuration for the components feature.
 *
 * A configuration of this feature must be registered for it to be active.
 *
 * Spring users can set the `botcommands.components.enable` property to `false` to disable this feature,
 * as adding the dependency will enable it by default.
 *
 * ## Database requirements
 * The `BotCommands-database` module must be configured, see [BDatabaseConfig].
 *
 * ### Compatible RDBMS
 * #### PostgreSQL
 * This is the database this feature is tested against, it is highly recommended using it.
 *
 * #### H2
 * H2 is supported, although not recommended, and requires the PostgreSQL compatibility mode.
 *
 * The JDBC URL of a file-based H2 DB might look like this:
 * `jdbc:h2:file:bc;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH`
 *
 * ## Setting up the database schema
 * The tables required to store components are defined by the scripts in `db/bc-migration/components`.
 *
 * It is recommended to use a migration tool to run these automatically, for example with Flyway:
 *
 * ```java
 * Flyway.configure(getClass().getClassLoader())
 *      .dataSource(source)
 *      .schemas("bc_components")
 *      .locations("db/bc-migration/components/generic", "db/bc-migration/components/<vendor>")
 *      .load()
 *      .migrate();
 * ```
 * This will run all the migration scripts required to set up your database,
 * where `<vendor>` is either `postgresql` or `h2`,
 * you can run this in the same class as your connection supplier.
 *
 * @see [BComponentsConfig.builder]
 * @see [registerComponents]
 */
@InjectedService
interface BComponentsConfig : IConfig, BComponentsConfigProps {

    override val configType get() = BComponentsConfig::class.java

    companion object {
        /**
         * Creates a new [BComponentsConfigBuilder], you must [build][BComponentsConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): BComponentsConfigBuilder {
            return BComponentsConfigBuilder.create()
        }
    }
}

interface BComponentsConfigProps {
}

/**
 * Builder of [BComponentsConfig].
 *
 * @see BComponentsConfig.builder
 */
@ConfigDSL
class BComponentsConfigBuilder private constructor() : BComponentsConfigProps {

    /**
     * Builds the [BComponentsConfig], you can register the built configuration with [BConfigBuilder.registerModule].
     */
    fun build() = object : BComponentsConfig {
    }

    internal companion object {
        @JvmSynthetic
        internal fun create(): BComponentsConfigBuilder = BComponentsConfigBuilder()
    }
}

/**
 * Registers the components feature.
 *
 * @param block A block for further configuration
 *
 * @see BComponentsConfig
 */
fun BConfigBuilder.registerComponents(block: BComponentsConfigBuilder.() -> Unit = { }) {
    val config = BComponentsConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
