package dev.freya02.botcommands.method.accessors.internal;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

@NullMarked
public class MethodArguments implements Cloneable {

    public static final Object NO_VALUE = new Object();

    private final @Nullable Object[] args;
    private int cursor = 0;

    // Built by the accessor
    public MethodArguments(int size) {
        final var args = new Object[size];
        Arrays.fill(args, NO_VALUE);
        this.args = args;
    }

    @Override
    public MethodArguments clone() {
        try {
            final MethodArguments clone = (MethodArguments) super.clone();
            clone.cursor = 0;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void push(@Nullable Object value) {
        args[cursor] = value;
        cursor += 1;
    }

    // TODO use `push` instead to avoid messing with offsets caused by instance parameters
    //  n.b. not all usages will be replaceable
    public void set(int index, @Nullable Object value) {
        args[index] = value;
    }

    @Nullable
    public Object get(int index) {
        return args[index];
    }

    public @Nullable Object[] getArgs() {
        return args;
    }

    public int size() {
        return args.length;
    }
}
