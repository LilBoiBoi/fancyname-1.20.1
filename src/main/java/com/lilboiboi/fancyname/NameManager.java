package com.lilboiboi.fancyname;

import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;

import java.awt.Color;
import java.util.HashMap;
import java.util.UUID;

public class NameManager {
    public static class NameData {
        public Color staticColor = null;
        public Color gradientStart = null;
        public Color gradientEnd = null;
        public boolean gradientAnimated = false;
        public boolean glow = false;
        public int gradientStep = 0;

        public NameData() {}
    }

    // Holds player UUIDs and their name data
    private static final HashMap<UUID, NameData> nameDataMap = new HashMap<>();

    public static NameData get(ServerPlayerEntity player) {
        return nameDataMap.computeIfAbsent(player.getUuid(), uuid -> new NameData());
    }

    public static void clear(ServerPlayerEntity player) {
        nameDataMap.remove(player.getUuid());
    }
}
