package com.pinbar;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Registers the client-side {@code /pin} command.
 *
 * <pre>
 * /pin &lt;x&gt; &lt;y&gt; &lt;z&gt;
 * /pin overworld|nether|end &lt;x&gt; &lt;y&gt; &lt;z&gt;
 * </pre>
 */
public final class PinCommand {
	private PinCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
		var root = ClientCommands.literal("pin");
		root.then(coordinates(null));
		root.then(ClientCommands.literal("overworld").then(coordinates(Level.OVERWORLD)));
		root.then(ClientCommands.literal("nether").then(coordinates(Level.NETHER)));
		root.then(ClientCommands.literal("end").then(coordinates(Level.END)));
		dispatcher.register(root);
	}

	private static RequiredArgumentBuilder<FabricClientCommandSource, CoordinateArgument.Coord> coordinates(
			ResourceKey<Level> dimension) {
		return ClientCommands.argument("x", CoordinateArgument.coordinate())
				.then(ClientCommands.argument("y", CoordinateArgument.coordinate())
						.then(ClientCommands.argument("z", CoordinateArgument.coordinate())
								.executes(context -> execute(context, dimension))));
	}

	private static int execute(CommandContext<FabricClientCommandSource> context, ResourceKey<Level> dimension) {
		FabricClientCommandSource source = context.getSource();
		var player = source.getPlayer();

		ResourceKey<Level> targetDimension = dimension != null
				? dimension
				: player.level().dimension();

		double x = resolve(context, "x", player.getX());
		double y = resolve(context, "y", player.getY());
		double z = resolve(context, "z", player.getZ());

		BlockPos pos = BlockPos.containing(x, y, z);
		boolean sameDimension = player.level().dimension().equals(targetDimension);

		PinManager.set(source.getClient(), targetDimension, pos);

		if (sameDimension) {
			source.sendFeedback(Component.translatable("pinbar.pin.set", pos.getX(), pos.getY(), pos.getZ()));
		} else {
			source.sendFeedback(Component.translatable("pinbar.pin.other_dimension",
					targetDimension.identifier().toString()));
		}

		return 1;
	}

	private static double resolve(CommandContext<FabricClientCommandSource> context, String name, double base) {
		CoordinateArgument.Coord coordinate = context.getArgument(name, CoordinateArgument.Coord.class);
		return coordinate.relative() ? base + coordinate.value() : coordinate.value();
	}
}
