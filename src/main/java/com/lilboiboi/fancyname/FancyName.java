package com.lilboiboi.fancyname;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader; // <-- NEW IMPORT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path; // <-- NEW IMPORT
import java.nio.file.Files; // <-- NEW IMPORT (for Files.exists and Files.createDirectories)
import java.io.IOException; // <-- NEW IMPORT (for handling IOException)

public class FancyName implements ModInitializer {
	public static final String MOD_ID = "fancyname";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// --- NEW: Define CONFIG_DIR for your mod's configuration files ---
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("FancyName mod initializing...");

		// --- NEW: Ensure the config directory exists ---
		if (!Files.exists(CONFIG_DIR)) {
			try {
				Files.createDirectories(CONFIG_DIR);
				LOGGER.info("Created config directory: {}", CONFIG_DIR);
			} catch (IOException e) {
				LOGGER.error("Failed to create config directory {}: {}", CONFIG_DIR, e.getMessage());
				// It's good practice to log the full stack trace for critical errors
				LOGGER.error("Stack trace:", e);
			}
		}

		// --- NEW: Load the name data when the mod initializes ---
		NameManager.load();

		FancyNameCommands.register(); // Assuming this registers server-side commands
		FancyNameTicker.register();   // Assuming this handles client-side ticks for animation

		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			FancyChatHandler.register(server); // This likely sets up chat event listeners on the server
		});

		// You might also want to register for a server shutdown event to save data reliably
		// Or ensure NameManager.save() is called when data is modified.
	}
}