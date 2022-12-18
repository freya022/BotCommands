package com.freya02.botcommands.othertests;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.RegexParameterResolver;
import com.freya02.botcommands.internal.commands.prefixed.CommandPattern;
import com.freya02.botcommands.internal.commands.prefixed.CommandPattern.ParameterPattern;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation;
import com.freya02.botcommands.internal.parameters.resolvers.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandPatternTest {
    private record TestParameterPattern(RegexParameterResolver<?, ?> resolver, boolean optional,
                                        boolean hasMultipleQuotable) {
        public ParameterPattern toParameterPattern() {
            return new ParameterPattern(resolver, optional, hasMultipleQuotable);
        }
    }

    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
    }

    private static void test(List<TestParameterPattern> patterns) {
        final String syntax = patterns.stream()
                .map(p -> p.resolver.getClass().getSimpleName().replace("Resolver", "") + (p.optional ? "?" : ""))
                .collect(Collectors.joining(" "));
        System.out.println("Syntax: " + syntax);

        final Pattern pattern = CommandPattern.joinPatterns(patterns.stream().map(TestParameterPattern::toParameterPattern).toList());
        System.out.println("Pattern: " + pattern);

        final String exampleStr = getExampleStr(patterns);
        System.out.println("Example: " + exampleStr);
        System.out.println("Matches: " + pattern.asMatchPredicate().test(exampleStr));

        System.out.println();
    }

    private static void test1() {
        test(List.of(
                new TestParameterPattern(new LibResolver(), true, false),
                new TestParameterPattern(new StringResolver(), false, false)
        ));
    }

    private static void test2() {
        test(List.of(
                new TestParameterPattern(new UserResolver(), false, false),
                new TestParameterPattern(new LongResolver(), true, false),
                new TestParameterPattern(new StringResolver(), true, false)
        ));
    }

    private static void test3() {
        test(List.of(
                new TestParameterPattern(new MemberResolver(), false, false),
                new TestParameterPattern(new StringResolver(), true, false)
        ));
    }

    private static void test4() {
        test(List.of(
                new TestParameterPattern(new LibResolver(), true, false),
                new TestParameterPattern(new StringResolver(), false, false),
                new TestParameterPattern(new DoubleResolver(), true, false)
        ));
    }

    private static String getExampleStr(List<TestParameterPattern> patterns) {
        return patterns.stream()
                .filter(t -> !t.optional)
                .map(t -> t.resolver)
                .map(RegexParameterResolver::getTestExample)
                .collect(Collectors.joining(" "));
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
            return "jda";
        }
    }
}
