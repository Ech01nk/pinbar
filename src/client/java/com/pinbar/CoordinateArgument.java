package com.pinbar;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.network.chat.Component;

/**
 * A single coordinate that accepts absolute numbers and {@code ~} relative
 * values (e.g. {@code 120}, {@code ~}, {@code ~-3.5}).
 */
public final class CoordinateArgument implements ArgumentType<CoordinateArgument.Coord> {
	private static final SimpleCommandExceptionType ERROR_EXPECTED_COORDINATE =
			new SimpleCommandExceptionType(Component.translatable("pinbar.command.expected_coordinate"));
	private static final Collection<String> EXAMPLES = List.of("0", "~", "~1", "-12.5");

	private CoordinateArgument() {
	}

	public static CoordinateArgument coordinate() {
		return new CoordinateArgument();
	}

	@Override
	public Coord parse(StringReader reader) throws CommandSyntaxException {
		if (!reader.canRead()) {
			throw ERROR_EXPECTED_COORDINATE.createWithContext(reader);
		}

		boolean relative = reader.peek() == '~';

		if (relative) {
			reader.skip();
		}

		double value = 0.0;

		if (reader.canRead() && isNumberStart(reader.peek())) {
			value = reader.readDouble();
		} else if (!relative) {
			throw ERROR_EXPECTED_COORDINATE.createWithContext(reader);
		}

		return new Coord(relative, value);
	}

	private static boolean isNumberStart(char c) {
		return c == '-' || c == '+' || c == '.' || Character.isDigit(c);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (builder.getRemaining().isEmpty()) {
			builder.suggest("~");
		}

		return builder.buildFuture();
	}

	public record Coord(boolean relative, double value) {
	}
}
