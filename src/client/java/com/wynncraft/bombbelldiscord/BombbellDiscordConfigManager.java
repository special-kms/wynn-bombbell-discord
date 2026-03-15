package com.wynncraft.bombbelldiscord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class BombbellDiscordConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("bombbelldiscord.json");
    private BombbellDiscordConfig config = new BombbellDiscordConfig();

    public BombbellDiscordConfig load() {
        if (Files.notExists(configPath)) {
            config.sanitize();
            save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            BombbellDiscordConfig loaded = GSON.fromJson(reader, BombbellDiscordConfig.class);
            config = loaded == null ? new BombbellDiscordConfig() : loaded;
            config.sanitize();
        } catch (IOException | JsonParseException exception) {
            config = new BombbellDiscordConfig();
            config.sanitize();
            save();
        }

        return config;
    }

    public BombbellDiscordConfig get() {
        config.sanitize();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {
        }
    }
}
