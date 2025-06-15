package com.lilboiboi.fancyname;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FancyName implements ModInitializer {
	public static final String MOD_ID = "fancyname";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("FancyName mod initializing...");

		FancyNameCommands.register();
		FancyNameTicker.register();

		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			FancyChatHandler.register(server);
		});
	}
}
