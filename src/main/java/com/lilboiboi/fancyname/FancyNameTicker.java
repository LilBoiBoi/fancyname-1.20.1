package com.lilboiboi.fancyname;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class FancyNameTicker {
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                NameManager.NameData data = NameManager.get(player);
                if (data.gradientAnimated) {
                    data.gradientStep = (data.gradientStep + 1) % 100; // cycle every 100 ticks (5 sec at 20 TPS)
                }
            }
        });
    }

    public static int getGlobalTick() {
        return tickCounter;
    }
}
