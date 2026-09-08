package doc.java.examples.core.hooks.custom;

import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirements;
import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirementsProvider;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.jspecify.annotations.NullMarked;

@BService
@NullMarked
public class MyCustomEventRequirementsProvider implements CustomEventRequirementsProvider {
    // A custom event indicating a member moved to a different audio channel
    static class GuildVoiceChannelMoveEvent {
        // ...
    }

    @Override
    public CustomEventRequirements get(Class<?> handledEventType) {
        // Any event that is or extends GuildVoiceChannelMoveEvent
        if (GuildVoiceChannelMoveEvent.class.isAssignableFrom(handledEventType)) {
            // A member moving from a channel to another is signaled by a GuildVoiceUpdateEvent,
            //  so we give the same requirements
            return CustomEventRequirements.fromEvents(GuildVoiceUpdateEvent.class);
        }

        // For other events than those we directly support.
        // It can also be events from a different module,
        //  in which case that module should have a CustomEventRequirementsProvider too.
        return CustomEventRequirements.unknown();
    }
}
