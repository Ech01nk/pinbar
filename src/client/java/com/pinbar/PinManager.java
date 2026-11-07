package com.pinbar;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAssets;

/**
 * Holds the single active pin and keeps it in sync with the vanilla locator bar.
 *
 * <p>The pin is intentionally <b>not persisted</b>: it lives only in this
 * class for the duration of the client session, and is dropped on disconnect.
 * It is also only tracked while the player is in the pin's dimension.
 */
public final class PinManager {
	private static final UUID PIN_ID = UUID.nameUUIDFromBytes("pinbar:pin".getBytes(StandardCharsets.UTF_8));
	private static final String STYLE_ID = "pinbar:pin";

	private static Pin pin;
	private static TrackedWaypoint trackedWaypoint;
	private static int trackedY = Integer.MIN_VALUE;

	private PinManager() {
	}

	public record Pin(ResourceKey<Level> dimension, BlockPos pos) {
	}

	public static void set(Minecraft client, ResourceKey<Level> dimension, BlockPos pos) {
		untrack(client);
		pin = new Pin(dimension, pos);

		LocalPlayer player = client.player;

		if (player != null && player.level().dimension().equals(dimension)) {
			track(client);
		}
	}

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;

		if (player == null) {
			// Disconnected or not in a world yet: no persistence across sessions.
			pin = null;
			trackedWaypoint = null;
			trackedY = Integer.MIN_VALUE;
			return;
		}

		if (pin == null) {
			return;
		}

		if (!player.level().dimension().equals(pin.dimension())) {
			// Cross-dimension pins are kept but not displayed.
			untrack(client);
			return;
		}

		BlockPos playerPos = player.blockPosition();

		if (Math.abs(playerPos.getX() - pin.pos().getX()) <= 1
				&& Math.abs(playerPos.getZ() - pin.pos().getZ()) <= 1) {
			pin = null;
			untrack(client);
			player.sendOverlayMessage(Component.translatable("pinbar.pin.arrived"));
			return;
		}

		if (trackedWaypoint == null) {
			track(client);
		} else if (trackedY != Mth.floor(player.getEyeY())) {
			update(client, Mth.floor(player.getEyeY()));
		}
	}

	private static void track(Minecraft client) {
		if (pin == null || client.getConnection() == null || client.player == null) {
			return;
		}

		int y = Mth.floor(client.player.getEyeY());
		TrackedWaypoint waypoint = TrackedWaypoint.setPosition(PIN_ID, icon(), new Vec3i(
				pin.pos().getX(),
				y,
				pin.pos().getZ()));

		client.getConnection().getWaypointManager().trackWaypoint(waypoint);
		trackedWaypoint = waypoint;
		trackedY = y;
	}

	private static void update(Minecraft client, int y) {
		if (pin == null || trackedWaypoint == null || client.getConnection() == null) {
			return;
		}

		TrackedWaypoint waypoint = TrackedWaypoint.setPosition(PIN_ID, icon(), new Vec3i(
				pin.pos().getX(),
				y,
				pin.pos().getZ()));

		client.getConnection().getWaypointManager().trackWaypoint(waypoint);
		trackedWaypoint = waypoint;
		trackedY = y;
	}

	private static void untrack(Minecraft client) {
		if (trackedWaypoint != null && client.getConnection() != null) {
			client.getConnection().getWaypointManager().untrackWaypoint(trackedWaypoint);
		}

		trackedWaypoint = null;
		trackedY = Integer.MIN_VALUE;
	}

	private static Waypoint.Icon icon() {
		Waypoint.Icon icon = new Waypoint.Icon();
		icon.style = WaypointStyleAssets.createId(STYLE_ID);
		icon.color = Optional.of(0xFFFFFFFF);
		return icon;
	}
}
