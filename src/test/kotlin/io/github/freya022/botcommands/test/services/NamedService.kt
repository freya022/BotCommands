package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.internal.core.service.RequiresDefaultInjection

@InterfacedService(acceptMultiple = true)
interface INamedService

@BService(name = "modifiedNamedService")
@RequiresDefaultInjection
class NamedService1 : INamedService

@BService(name = "normalNamedService")
@RequiresDefaultInjection
class NamedService2 : INamedService