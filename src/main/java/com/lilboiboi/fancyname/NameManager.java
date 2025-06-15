package com.lilboiboi.fancyname;

import com.google.gson.Gson; // Assuming Gson is used for serialization/deserialization
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.player.PlayerEntity; // Client-side PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity; // Server-side ServerPlayerEntity
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import java.awt.Color;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DATA_FILE = FancyName.CONFIG_DIR.resolve("fancy_names.json");
    private static final Map<UUID, NameData> PLAYER_NAME_DATA = new HashMap<>();

    // Called on server side, when a player joins or data is updated
    public static void set(ServerPlayerEntity player, NameData data) {
        PLAYER_NAME_DATA.put(player.getUuid(), data);
        save(); // Save immediately or debounce
    }

    // --- NEW / MODIFIED: This method is now accessible from both server and client ---
    // For server-side operations where you have ServerPlayerEntity
    public static NameData get(ServerPlayerEntity player) {
        return PLAYER_NAME_DATA.computeIfAbsent(player.getUuid(), uuid -> new NameData());
    }

    // For client-side operations where you only have PlayerEntity (or just the UUID)
    public static NameData get(PlayerEntity player) {
        return PLAYER_NAME_DATA.computeIfAbsent(player.getUuid(), uuid -> new NameData());
    }

    // Most robust for client-side as PlayerEntity might not always be available
    public static NameData get(UUID uuid) {
        return PLAYER_NAME_DATA.computeIfAbsent(uuid, u -> new NameData());
    }
    // --- END NEW / MODIFIED ---

    public static void load() {
        if (!Files.exists(DATA_FILE)) {
            FancyName.LOGGER.info("FancyName data file not found, creating new one.");
            save(); // Create an empty file
            return;
        }

        try (FileReader reader = new FileReader(DATA_FILE.toFile())) {
            NameDataWrapper wrapper = GSON.fromJson(reader, NameDataWrapper.class);
            if (wrapper != null && wrapper.playerData != null) {
                PLAYER_NAME_DATA.clear();
                PLAYER_NAME_DATA.putAll(wrapper.playerData);
                FancyName.LOGGER.info("Loaded {} fancy name entries.", PLAYER_NAME_DATA.size());
            }
        } catch (IOException | JsonSyntaxException e) {
            FancyName.LOGGER.error("Failed to load fancy name data from {}: {}", DATA_FILE, e.getMessage());
            // Optionally, backup corrupt file and create a new one
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(DATA_FILE.toFile())) {
            GSON.toJson(new NameDataWrapper(PLAYER_NAME_DATA), writer);
        } catch (IOException e) {
            FancyName.LOGGER.error("Failed to save fancy name data to {}: {}", DATA_FILE, e.getMessage());
        }
    }

    // Inner class for Gson serialization of the map
    private static class NameDataWrapper {
        Map<UUID, NameData> playerData;

        public NameDataWrapper(Map<UUID, NameData> playerData) {
            this.playerData = playerData;
        }
    }

    public static class NameData {
        public Color staticColor = null;
        public Color gradientStart = null;
        public Color gradientEnd = null;
        public boolean gradientAnimated = false;
        public int gradientStep = 0; // For animation tick
        public boolean glow = false; // For glow effect

        // Default constructor for Gson
        public NameData() {}

        // You might want constructors for easier data creation
        public NameData(Color staticColor) {
            this.staticColor = staticColor;
        }

        public NameData(Color gradientStart, Color gradientEnd, boolean gradientAnimated) {
            this.gradientStart = gradientStart;
            this.gradientEnd = gradientEnd;
            this.gradientAnimated = gradientAnimated;
        }

        // Method to update gradient step (call this on client tick or similar)
        public void updateGradientStep() {
            this.gradientStep = (this.gradientStep + 1) % 1000; // Increment and wrap
        }

        @Override
        public String toString() {
            return "NameData{" +
                    "staticColor=" + staticColor +
                    ", gradientStart=" + gradientStart +
                    ", gradientEnd=" + gradientEnd +
                    ", gradientAnimated=" + gradientAnimated +
                    ", gradientStep=" + gradientStep +
                    ", glow=" + glow +
                    '}';
        }
    }
}