package com.freya02.botcommands.othertests;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.internal.commands.prefixed.CommandPattern;
import com.freya02.botcommands.internal.commands.prefixed.CommandPattern.ParameterPattern;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation;
import com.freya02.botcommands.internal.parameters.resolvers.LongResolver;
import com.freya02.botcommands.internal.parameters.resolvers.StringResolver;
import com.freya02.botcommands.internal.parameters.resolvers.UserResolver;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public class CommandPatternTest {
    public static void main(String[] args) {
        test1();
        test2();
    }

    private static void test1() {
        final List<ParameterPattern> patterns = List.of(
                new ParameterPattern(new LibResolver(), true, false),
                new ParameterPattern(new StringResolver(), false, false)
        );

        System.out.println(CommandPattern.joinPatterns(patterns));
    }

    private static void test2() {
        final List<ParameterPattern> patterns = List.of(
                new ParameterPattern(new UserResolver(), false, false),
                new ParameterPattern(new LongResolver(), true, false),
                new ParameterPattern(new StringResolver(), true, false)
        );

        System.out.println(CommandPattern.joinPatterns(patterns));
    }

    private static class LibResolver extends ParameterResolver<LibResolver, Object> implements RegexParameterResolver<LibResolver, Object> {
        public LibResolver() {
            super(Object.class);
        }

        @Nullable
        @Override
        public Object resolve(@NotNull BContext context, @NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String[] args) {
            return null;
        }

        @Override
        public @NotNull Pattern getPattern() {
            return Pattern.compile("(?i)(JDA|java|BotCommands|BC)(?-i)");
        }

        @Override
        public @NotNull String getTestExample() {
            return null;
        }
    }
}
