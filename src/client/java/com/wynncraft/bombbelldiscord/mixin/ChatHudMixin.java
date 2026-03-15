package com.wynncraft.bombbelldiscord.mixin;

import com.wynncraft.bombbelldiscord.BombbellDiscordClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void bombbelldiscord$captureSimpleMessage(Text message, CallbackInfo ci) {
        capture(message);
    }

    @Inject(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
        at = @At("HEAD")
    )
    private void bombbelldiscord$captureSignedMessage(
        Text message,
        MessageSignatureData signature,
        MessageIndicator indicator,
        CallbackInfo ci
    ) {
        capture(message);
    }

    private void capture(Text message) {
        if (BombbellDiscordClient.captureService() == null || message == null) {
            return;
        }
        BombbellDiscordClient.captureService().captureChatText(message.getString());
    }
}
