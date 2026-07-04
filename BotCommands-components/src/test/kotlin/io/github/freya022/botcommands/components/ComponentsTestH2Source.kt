package io.github.freya022.botcommands.components

import io.github.freya022.botcommands.api.components.utils.ComponentsSchemaMigrator
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.helpers.db.TestH2Source

@BService
class ComponentsTestH2Source : TestH2Source() {
    init {
        ComponentsSchemaMigrator.of(source).migrate()
    }
}
