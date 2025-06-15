package com.lilboiboi.fancyname.mixin;

import com.lilboiboi.fancyname.NameManager;
import com.lilboiboi.fancyname.NameManager.NameData;
import com.lilboiboi.fancyname.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
// Import AbstractClientPlayerEntity
import net.minecraft.client.network.AbstractClientPlayerEntity; // <-- NEW IMPORT
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    // Note: The superclass type parameter also needs to match the mixin target's entity type.
    // So, PlayerEntity here also becomes AbstractClientPlayerEntity.

    public PlayerEntityRendererMixin(EntityRendererFactory.Context context, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    // Change PlayerEntity to AbstractClientPlayerEntity here
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    protected void injectRenderLabel(AbstractClientPlayerEntity player, Text text, MatrixStack matrices, // <-- Changed 'PlayerEntity player'
                                     VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        NameData data = NameManager.get(player.getUuid());

        String name = player.getName().getString();
        int nameLength = name.length();
        float tick = data.gradientStep / 100.0f; // Ensure data.gradientStep is updated elsewhere (e.g., tick event)

        // Fancy name rendering
        MutableText builder = Text.literal("");

        if (data.gradientStart != null && data.gradientEnd != null) {
            for (int i = 0; i < nameLength; i++) {
                float t = (i + (data.gradientAnimated ? (tick * nameLength) : 0)) / (float) nameLength;
                t = t % 1.0f;
                if (t < 0) t += 1.0f;

                Color c = ColorUtil.interpolate(data.gradientStart, data.gradientEnd, t);
                builder.append(Text.literal(String.valueOf(name.charAt(i))).styled(style ->
                        style.withColor(c.getRGB())
                ));
            }
        } else if (data.staticColor != null) {
            builder.append(Text.literal(name).styled(style ->
                    style.withColor(data.staticColor.getRGB())
            ));
        } else {
            builder.append(player.getName());
        }

        // Custom nametag rendering logic
        double distanceSq = this.dispatcher.getSquaredDistanceToCamera(player);
        if (distanceSq < 4096.0D && !player.isInvisible()) {
            float offsetY = player.getHeight() + 0.5F;
            matrices.push();
            matrices.translate(0.0D, offsetY, 0.0D);
            matrices.multiply(this.dispatcher.getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            int baseColor;
            if (data.glow && data.gradientStart != null && data.gradientEnd != null) {
                float glowT = (data.gradientStep % 100) / 100.0f;
                Color glowColor = ColorUtil.interpolate(data.gradientStart, data.gradientEnd, glowT);
                baseColor = glowColor.getRGB();
            } else if (data.glow) {
                baseColor = 0xFFFFFF;
            } else {
                baseColor = 0xA0A0A0;
            }

            int opacity = (int)(MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            MinecraftClient.getInstance().textRenderer.draw(
                    builder,
                    -MinecraftClient.getInstance().textRenderer.getWidth(builder) / 2f,
                    0f,
                    baseColor | opacity,
                    false,
                    matrix,
                    vertexConsumers,
                    TextLayerType.NORMAL,
                    0,
                    light
            );

            matrices.pop();
        }

        ci.cancel();
    }
}