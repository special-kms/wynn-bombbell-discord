package com.wynncraft.bombbelldiscord;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class BombbellDiscordClient implements ClientModInitializer {
    private static BombCaptureService captureService;

    private KeyBinding exportKeyBinding;

    public static BombCaptureService captureService() {
        return captureService;
    }

    @Override
    public void onInitializeClient() {
        BombbellDiscordConfigManager configManager = new BombbellDiscordConfigManager();
        configManager.load();
        BombbellDiscordConfig config = configManager.get();

        captureService = new BombCaptureService(
            configManager,
            new BombBellParser(),
            new BombSnapshotFormatter()
        );

        exportKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bombbelldiscord.export",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            KeyBinding.Category.MISC
        ));
        exportKeyBinding.setBoundKey(parseConfiguredKey(config.exportKey));
        KeyBinding.updateKeysByCode();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> captureService.captureChatText(message.getString()));
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
            captureService.captureChatText(message.getString())
        );

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("bombcopy").executes(context -> {
                captureService.requestFreshExport(context.getSource().getClient());
                return 1;
            }))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            captureService.tick(client);
            while (exportKeyBinding.wasPressed()) {
                captureService.requestFreshExport(client);
            }
        });
    }

    private static InputUtil.Key parseConfiguredKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return InputUtil.UNKNOWN_KEY;
        }

        try {
            return InputUtil.fromTranslationKey(rawKey.trim());
        } catch (IllegalArgumentException exception) {
            return InputUtil.UNKNOWN_KEY;
        }
    }
}
