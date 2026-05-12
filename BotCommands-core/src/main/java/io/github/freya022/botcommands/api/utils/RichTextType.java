package io.github.freya022.botcommands.api.utils;

import net.dv8tion.jda.api.entities.Message.MentionType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

@NullMarked
public enum RichTextType {
    USER(MentionType.USER),
    EMOJI(MentionType.EMOJI),
    CHANNEL(MentionType.CHANNEL),
    ROLE(MentionType.ROLE),
    EVERYONE(MentionType.EVERYONE),
    HERE(MentionType.HERE),
    UNICODE_EMOTE(null),
    URL(null),
    TEXT(null);

    @Nullable
    private final MentionType mentionType;

    RichTextType(@Nullable MentionType mentionType) {
        this.mentionType = mentionType;
    }

    @Nullable
    public MentionType getMentionType() {
        return mentionType;
    }

    @Nullable
    public Pattern getPattern() {
        if (mentionType == null) return null;
        return mentionType.getPattern();
    }
}
