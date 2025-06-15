package com.lilboiboi.fancyname;

import com.lilboiboi.fancyname.NameManager.NameData;
import com.lilboiboi.fancyname.util.ColorUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.awt.Color;

public class FancyChatHandler {
    /**
     * Call this once on server startup to hook into chat.
     * Leave blank or implement your chat‐networking registration here.
     */
    public static void register(MinecraftServer server) {
        // e.g. ServerPlayNetworking.registerGlobalReceiver(…);
    }

    @SuppressWarnings("unused")
    private static void handleChatMessage(MinecraftServer server, ServerPlayerEntity player, String messageContent) {
        NameData data = NameManager.get(player);
        String name = player.getName().getString();

        // Build colored name
        MutableText nameBuilder = Text.literal("");
        if (data.gradientStart != null && data.gradientEnd != null) {
            for (int i = 0; i < name.length(); i++) {
                float t = (i + (data.gradientAnimated ? data.gradientStep * 0.01f * name.length() : 0))
                        / (float) name.length();
                if (t > 1) t -= 1;
                Color c = ColorUtil.interpolate(data.gradientStart, data.gradientEnd, t);
                nameBuilder.append(
                        Text.literal(String.valueOf(name.charAt(i)))
                                .styled(s -> s.withColor(TextColor.fromRgb(c.getRGB())))
                );
            }
        } else if (data.staticColor != null) {
            nameBuilder.append(
                    Text.literal(name)
                            .styled(s -> s.withColor(TextColor.fromRgb(data.staticColor.getRGB())))
            );
        } else {
            nameBuilder.append(Text.literal(name));
        }

        // Broadcast
        Text fullMessage = Text.translatable(
                "chat.type.text",
                nameBuilder,
                Text.literal(messageContent)
        );
        server.getPlayerManager().broadcast(fullMessage, false);
    }
}
