package com.pinbar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Client entrypoint for Pinbar.
 *
 * <p>Everything in this mod runs on the client only: the pin is a local
 * waypoint tracked by {@code ClientWaypointManager}, so the server never
 * knows about it and other players never see it.
 */
public final class PinbarClient implements ClientModInitializer {
	public static final String MOD_ID = "pinbar";

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register(PinCommand::register);
		ClientTickEvents.END_CLIENT_TICK.register(PinManager::tick);
	}
}
