package com.freya02.botcommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.concurrent.DelayedCompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

public class MessageActionWrapper implements MessageAction {
	private final MessageAction action;
	private final Consumer<? super Throwable> onException;

	public MessageActionWrapper(MessageAction action, Consumer<? super Throwable> onException) {
		this.action = action;
		this.onException = onException;
	}

	public static boolean isPassContext() {
		return RestAction.isPassContext();
	}

	public static void setPassContext(boolean enable) {
		RestAction.setPassContext(enable);
	}

	public static void setDefaultTimeout(long timeout, @NotNull TimeUnit unit) {
		RestAction.setDefaultTimeout(timeout, unit);
	}

	public static long getDefaultTimeout() {
		return RestAction.getDefaultTimeout();
	}

	@Nonnull
	public static Consumer<? super Throwable> getDefaultFailure() {
		return RestAction.getDefaultFailure();
	}

	public static void setDefaultFailure(@Nullable Consumer<? super Throwable> callback) {
		RestAction.setDefaultFailure(callback);
	}

	@Nonnull
	public static Consumer<Object> getDefaultSuccess() {
		return RestAction.getDefaultSuccess();
	}

	public static void setDefaultSuccess(@Nullable Consumer<Object> callback) {
		RestAction.setDefaultSuccess(callback);
	}

	@Override
	@Nonnull
	public MessageAction setCheck(@Nullable BooleanSupplier checks) {
		return action.setCheck(checks);
	}

	@Override
	@Nonnull
	public MessageAction timeout(long timeout, @NotNull TimeUnit unit) {
		return action.timeout(timeout, unit);
	}

	@Override
	@Nonnull
	public MessageAction deadline(long timestamp) {
		return action.deadline(timestamp);
	}

	@Override
	@Nonnull
	public MessageChannel getChannel() {
		return action.getChannel();
	}

	@Override
	public boolean isEmpty() {
		return action.isEmpty();
	}

	@Override
	public boolean isEdit() {
		return action.isEdit();
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction apply(@Nullable Message message) {
		return action.apply(message);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction tts(boolean isTTS) {
		return action.tts(isTTS);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction reset() {
		return action.reset();
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction nonce(@Nullable String nonce) {
		return action.nonce(nonce);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction content(@Nullable String content) {
		return action.content(content);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction embed(@Nullable MessageEmbed embed) {
		return action.embed(embed);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction append(@NotNull CharSequence csq) {
		return action.append(csq);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction append(@Nullable CharSequence csq, int start, int end) {
		return action.append(csq, start, end);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction append(char c) {
		return action.append(c);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction appendFormat(@NotNull String format, Object... args) {
		return action.appendFormat(format, args);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction addFile(@NotNull InputStream data, @NotNull String name, @NotNull AttachmentOption... options) {
		return action.addFile(data, name, options);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction addFile(@NotNull byte[] data, @NotNull String name, @NotNull AttachmentOption... options) {
		return action.addFile(data, name, options);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction addFile(@NotNull File file, @NotNull AttachmentOption... options) {
		return action.addFile(file, options);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction addFile(@NotNull File file, @NotNull String name, @NotNull AttachmentOption... options) {
		return action.addFile(file, name, options);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction clearFiles() {
		return action.clearFiles();
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction clearFiles(@NotNull BiConsumer<String, InputStream> finalizer) {
		return action.clearFiles(finalizer);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction clearFiles(@NotNull Consumer<InputStream> finalizer) {
		return action.clearFiles(finalizer);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public MessageAction override(boolean bool) {
		return action.override(bool);
	}

	@Override
	@Nonnull
	public JDA getJDA() {
		return action.getJDA();
	}

	@Override
	public void queue() {
		action.queue();
	}

	@Override
	public void queue(@Nullable Consumer<? super Message> success, @Nullable Consumer<? super Throwable> failure) {
		action.queue(success, failure);
	}

	@Override
	public Message complete() {
		return action.complete();
	}

	@Override
	public Message complete(boolean shouldQueue) throws RateLimitedException {
		return action.complete(shouldQueue);
	}

	@Override
	@Nonnull
	public CompletableFuture<Message> submit() {
		return action.submit();
	}

	@Override
	@Nonnull
	public CompletableFuture<Message> submit(boolean shouldQueue) {
		return action.submit(shouldQueue);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public <O> RestAction<O> map(@NotNull Function<? super Message, ? extends O> map) {
		return action.map(map);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public <O> RestAction<O> flatMap(@NotNull Function<? super Message, ? extends RestAction<O>> flatMap) {
		return action.flatMap(flatMap);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public <O> RestAction<O> flatMap(@Nullable Predicate<? super Message> condition, @NotNull Function<? super Message, ? extends RestAction<O>> flatMap) {
		return action.flatMap(condition, flatMap);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> delay(@NotNull Duration duration) {
		return action.delay(duration);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> delay(@NotNull Duration duration, @Nullable ScheduledExecutorService scheduler) {
		return action.delay(duration, scheduler);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> delay(long delay, @NotNull TimeUnit unit) {
		return action.delay(delay, unit);
	}

	@Override
	@CheckReturnValue
	@Nonnull
	public RestAction<Message> delay(long delay, @NotNull TimeUnit unit, @Nullable ScheduledExecutorService scheduler) {
		return action.delay(delay, unit, scheduler);
	}

	@Override
	@Nonnull
	public DelayedCompletableFuture<Message> submitAfter(long delay, @NotNull TimeUnit unit) {
		return action.submitAfter(delay, unit);
	}

	@Override
	@Nonnull
	public DelayedCompletableFuture<Message> submitAfter(long delay, @NotNull TimeUnit unit, @Nullable ScheduledExecutorService executor) {
		return action.submitAfter(delay, unit, executor);
	}

	@Override
	public Message completeAfter(long delay, @NotNull TimeUnit unit) {
		return action.completeAfter(delay, unit);
	}

	@Override
	@Nonnull
	public ScheduledFuture<?> queueAfter(long delay, @NotNull TimeUnit unit) {
		return action.queueAfter(delay, unit);
	}

	@Override
	@Nonnull
	public ScheduledFuture<?> queueAfter(long delay, @NotNull TimeUnit unit, @Nullable Consumer<? super Message> success) {
		return action.queueAfter(delay, unit, success);
	}

	@Override
	@Nonnull
	public ScheduledFuture<?> queueAfter(long delay, @NotNull TimeUnit unit, @Nullable Consumer<? super Message> success, @Nullable Consumer<? super Throwable> failure) {
		return action.queueAfter(delay, unit, success, failure);
	}

	@Override
	@Nonnull
	public ScheduledFuture<?> queueAfter(long delay, @NotNull TimeUnit unit, @Nullable ScheduledExecutorService executor) {
		return action.queueAfter(delay, unit, executor);
	}

	@Override
	@Nonnull
	public ScheduledFuture<?> queueAfter(long delay, @NotNull TimeUnit unit, @Nullable Consumer<? super Message> success, @Nullable ScheduledExecutorService executor) {
		return action.queueAfter(delay, unit, success, executor);
	}

	@Override
	@Nonnull
	public ScheduledFuture<?> queueAfter(long delay, @NotNull TimeUnit unit, @Nullable Consumer<? super Message> success, @Nullable Consumer<? super Throwable> failure, @Nullable ScheduledExecutorService executor) {
		return action.queueAfter(delay, unit, success, failure, executor);
	}

	@Override
	public void queue(@Nullable Consumer<? super Message> success) {
		queue(success, onException);
	}
}
