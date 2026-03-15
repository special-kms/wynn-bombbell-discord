package com.wynncraft.bombbelldiscord.mixin;

import com.wynncraft.bombbelldiscord.BombbellDiscordClient;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V", at = @At("HEAD"))
    private void bombbelldiscord$captureText(
        TextRenderer textRenderer,
        Text text,
        int x,
        int y,
        int color,
        boolean shadow,
        CallbackInfo ci
    ) {
        if (shouldCapture()) {
            BombbellDiscordClient.captureService().captureScreenText(text.getString());
        }
    }

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)V", at = @At("HEAD"))
    private void bombbelldiscord$captureOrderedText(
        TextRenderer textRenderer,
        OrderedText text,
        int x,
        int y,
        int color,
        boolean shadow,
        CallbackInfo ci
    ) {
        if (shouldCapture()) {
            BombbellDiscordClient.captureService().captureScreenText(orderedTextToString(text));
        }
    }

    @Inject(
        method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V",
        at = @At("HEAD")
    )
    private void bombbelldiscord$captureTooltip(
        TextRenderer textRenderer,
        List<Text> lines,
        Optional<TooltipData> data,
        int x,
        int y,
        CallbackInfo ci
    ) {
        if (shouldCapture()) {
            BombbellDiscordClient.captureService().captureTooltipText(lines);
        }
    }

    private boolean shouldCapture() {
        return BombbellDiscordClient.captureService() != null && MinecraftClient.getInstance().currentScreen != null;
    }

    private String orderedTextToString(OrderedText text) {
        StringBuilder builder = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }
}
