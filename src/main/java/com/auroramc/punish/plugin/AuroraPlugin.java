package com.auroramc.punish.plugin;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public abstract class AuroraPlugin extends Plugin {

    public abstract void load();
    public abstract void enable();
    public abstract void disable();

    @Override
    public void onEnable() {
        this.enable();
    }

    @Override
    public void onDisable() {
        this.disable();
    }

    @Override
    public void onLoad() {
        this.load();
    }

    public void sendMessage(String message) {
        ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("§a[" + this.getDescription().getName() + "] " + message));
    }

    public void sendMessage(String message, char color) {
        ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("§" + color + "[" + this.getDescription().getName() + "] " + message));
    }

    public void saveDefaultConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                sendMessage("Arquivo config.yml padrão salvo.");
            } catch (IOException e) {
                sendMessage("Não foi possível salvar config.yml: " + e.getMessage(), 'c');
            }
        }
    }

    public Configuration loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            try {
                return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            } catch (IOException e) {
                sendMessage("Não foi possível carregar config.yml: " + e.getMessage(), 'c');
            }
        }
        return null;
    }
}
