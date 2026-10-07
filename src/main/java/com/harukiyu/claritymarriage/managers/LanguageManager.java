package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages localized MiniMessage strings loaded from language.yml.
 */
public class LanguageManager {

    private final ClarityMarriage plugin;
    private final File langFile;
    private YamlConfiguration config;
    private final MiniMessage miniMessage;

    public LanguageManager(ClarityMarriage plugin) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder(), "language.yml");
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void load() {
        if (!langFile.exists()) {
            plugin.saveResource("language.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getRaw(String key) {
        return config.getString(key, "<red>Missing translation: " + key + "</red>");
    }

    public Component getComponent(String key, TagResolver... resolvers) {
        String raw = getRaw(key);
        return miniMessage.deserialize(raw, resolvers);
    }

    public Component getPrefixedComponent(String key, TagResolver... resolvers) {
        String prefix = getRaw("prefix");
        String raw = getRaw(key);
        return miniMessage.deserialize(prefix + raw, resolvers);
    }

    public void sendMessage(CommandSender sender, String key) {
        if (sender == null) return;
        sender.sendMessage(getPrefixedComponent(key));
    }

    public void sendMessage(CommandSender sender, String key, String... pairs) {
        if (sender == null) return;
        sender.sendMessage(getPrefixedComponent(key, createResolvers(pairs)));
    }

    public void sendMessageTags(CommandSender sender, String key, TagResolver... resolvers) {
        if (sender == null) return;
        sender.sendMessage(getPrefixedComponent(key, resolvers));
    }

    public void sendRawMessage(CommandSender sender, String key) {
        if (sender == null) return;
        sender.sendMessage(getComponent(key));
    }

    public void sendRawMessage(CommandSender sender, String key, String... pairs) {
        if (sender == null) return;
        sender.sendMessage(getComponent(key, createResolvers(pairs)));
    }

    public void sendRawMessageTags(CommandSender sender, String key, TagResolver... resolvers) {
        if (sender == null) return;
        sender.sendMessage(getComponent(key, resolvers));
    }

    public void broadcast(String key) {
        Bukkit.broadcast(getComponent(key));
    }

    public void broadcast(String key, String... pairs) {
        Bukkit.broadcast(getComponent(key, createResolvers(pairs)));
    }

    public void broadcastTags(String key, TagResolver... resolvers) {
        Bukkit.broadcast(getComponent(key, resolvers));
    }

    /**
     * Helper to construct TagResolvers from key-value pairs.
     * Usage: createResolvers("target", "Alex", "time", "10")
     */
    public TagResolver[] createResolvers(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return new TagResolver[0];
        }
        List<TagResolver> list = new ArrayList<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            String key = pairs[i];
            String val = pairs[i + 1];
            if (key != null && val != null) {
                list.add(Placeholder.parsed(key, val));
            }
        }
        return list.toArray(TagResolver[]::new);
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
