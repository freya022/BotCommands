package io.github.freya022.botcommands.internal.parameters.resolvers.users

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Resolver
@RequiresComponents
internal class ComponentInputUserResolver(
    context: BContext,
) : AbstractInputUserResolver<ComponentInputUserResolver>(),
    ComponentParameterResolver<ComponentInputUserResolver, InputUser> {

    private val messagesFactory: BotCommandsMessagesFactory = context.getService()

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): InputUser? {
        val id = data.asString().let {
            it.toLongOrNull() ?: throwArgument("Invalid user id: $it")
        }
        val entity = retrieveOrNull(id, event.message)
        if (entity == null)
            event.reply(messagesFactory.get(event).resolverUserNotFound(event, id)).setEphemeral(true).queue()

        return entity
    }

    override fun serialize(obj: InputUser) = SerializedComponentData.fromString(obj.id)
}
