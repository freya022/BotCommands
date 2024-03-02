package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

@InterfacedService(acceptMultiple = true)
interface INamedService

@BService(name = "modifiedNamedService")
class NamedService1 : INamedService

@BService(name = "normalNamedService")
class NamedService2 : INamedService