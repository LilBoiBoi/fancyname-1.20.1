package com.lilboiboi.fancyname.mixin;

import com.lilboiboi.fancyname.NameManager;
import com.lilboiboi.fancyname.NameManager.NameData;
import com.lilboiboi.fancyname.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context context, PlayerEntityModel<PlayerEntity> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    protected void injectRenderLabel(PlayerEntity player, Text text, MatrixStack matrices,
                                     VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        NameData data = NameManager.get(serverPlayer);

        String name = player.getName().getString();
        int nameLength = name.length();
        float tick = data.gradientStep / 100.0f;

        // Fancy name rendering
        MutableText builder = Text.literal("");

        if (data.gradientStart != null && data.gradientEnd != null) {
            for (int i = 0; i < nameLength; i++) {
                float t = (i + (data.gradientAnimated ? tick * nameLength : 0)) / (float) nameLength;
                if (t > 1) t -= 1;
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
            builder.append(Text.literal(name));
        }

        // Custom nametag rendering
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
                    TextRenderer.TextLayerType.NORMAL,
                    0,
                    light
            );

            matrices.pop();
        }

        ci.cancel();
    }
}