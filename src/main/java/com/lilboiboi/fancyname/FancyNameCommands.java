package com.lilboiboi.fancyname;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.awt.Color;

public class FancyNameCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /namecolor <hex>
            dispatcher.register(CommandManager.literal("namecolor")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("color", StringArgumentType.word())
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                String inputColor = StringArgumentType.getString(context, "color");
                                ServerPlayerEntity player = source.getPlayer();

                                try {
                                    Color parsedColor = Color.decode(inputColor.startsWith("#") ? inputColor : "#" + inputColor);
                                    NameManager.get(player).staticColor = parsedColor;
                                    source.sendFeedback(() -> Text.literal("✅ Set name color to " + inputColor), false);
                                } catch (Exception e) {
                                    source.sendError(Text.literal("❌ Invalid color format. Use hex like #FF00FF"));
                                }

                                return 1;
                            })
                    )
            );

            // /namegradient <start> <end> [animated]
            dispatcher.register(CommandManager.literal("namegradient")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("startColor", StringArgumentType.word())
                            .then(CommandManager.argument("endColor", StringArgumentType.word())
                                    .executes(ctx -> {
                                        return runGradient(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "startColor"),
                                                StringArgumentType.getString(ctx, "endColor"),
                                                false
                                        );
                                    })
                                    .then(CommandManager.argument("animated", StringArgumentType.word())
                                            .executes(ctx -> {
                                                boolean animated = Boolean.parseBoolean(StringArgumentType.getString(ctx, "animated"));
                                                return runGradient(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "startColor"),
                                                        StringArgumentType.getString(ctx, "endColor"),
                                                        animated
                                                );
                                            })
                                    )
                            )
                    )
            );

            // /nameglow <true|false>
            dispatcher.register(CommandManager.literal("nameglow")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("enabled", StringArgumentType.word())
                            .executes(ctx -> {
                                ServerCommandSource source = ctx.getSource();
                                ServerPlayerEntity player = source.getPlayer();
                                boolean glow = Boolean.parseBoolean(StringArgumentType.getString(ctx, "enabled"));

                                NameManager.get(player).glow = glow;
                                source.sendFeedback(() -> Text.literal("✨ Glow " + (glow ? "enabled" : "disabled")), false);
                                return 1;
                            })
                    )
            );
        });
    }

    private static int runGradient(ServerCommandSource source, String start, String end, boolean animated) {
        ServerPlayerEntity player = source.getPlayer();

        try {
            Color colorStart = Color.decode(start.startsWith("#") ? start : "#" + start);
            Color colorEnd = Color.decode(end.startsWith("#") ? end : "#" + end);

            NameManager.NameData data = NameManager.get(player);
            data.gradientStart = colorStart;
            data.gradientEnd = colorEnd;
            data.gradientAnimated = animated;

            source.sendFeedback(() ->
                    Text.literal("✅ Gradient set from " + start + " to " + end + (animated ? " (animated)" : "")), false);
        } catch (Exception e) {
            source.sendError(Text.literal("❌ Invalid color format. Use hex like #FF00FF"));
        }

        return 1;
    }
}
