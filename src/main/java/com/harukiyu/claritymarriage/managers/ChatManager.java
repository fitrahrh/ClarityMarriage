package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages private couple chat mode and direct messaging between partners.
 */
public class ChatManager {

    private final ClarityMarriage plugin;
    private final Set<UUID> coupleChatUsers = ConcurrentHashMap.newKeySet();

    public ChatManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public boolean isChatEnabled(UUID uuid) {
        return coupleChatUsers.contains(uuid);
    }

    public void disableChat(UUID uuid) {
        coupleChatUsers.remove(uuid);
    }

    public void toggleChat(Player player) {
        UUID uuid = player.getUniqueId();
        if (!plugin.getMarriageManager().isMarried(uuid)) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return;
        }

        Player partner = plugin.getMarriageManager().getPartnerPlayer(uuid);

        if (coupleChatUsers.contains(uuid)) {
            coupleChatUsers.remove(uuid);
            plugin.getLanguageManager().sendMessage(player, "chat-toggled-off");
        } else {
            String partnerName = partner != null ? partner.getName() : "Partner";
            coupleChatUsers.add(uuid);
            plugin.getLanguageManager().sendMessage(player, "chat-toggled-on", "partner", partnerName);
        }
    }

    public void sendCoupleMessage(Player sender, Player partner, String message) {
        String format = plugin.getConfigManager().getChatFormat();
        MiniMessage mm = plugin.getLanguageManager().getMiniMessage();

        Component component = mm.deserialize(format,
                Placeholder.parsed("sender", sender.getName()),
                Placeholder.parsed("message", message)
        );

        sender.sendMessage(component);
        partner.sendMessage(component);

        if (plugin.getConfigManager().isChatPlaySound()) {
            partner.playSound(partner.getLocation(), plugin.getConfigManager().getChatSound(), 1.0f, 1.2f);
        }
    }
}
