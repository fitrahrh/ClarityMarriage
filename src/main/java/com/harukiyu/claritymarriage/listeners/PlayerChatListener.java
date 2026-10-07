package com.harukiyu.claritymarriage.listeners;

import com.harukiyu.claritymarriage.ClarityMarriage;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Intercepts AsyncChatEvent to route chat into private couple channel when toggled.
 */
public class PlayerChatListener implements Listener {

    private final ClarityMarriage plugin;

    public PlayerChatListener(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!plugin.getChatManager().isChatEnabled(uuid)) {
            return;
        }

        // Intercept message for couple chat
        event.setCancelled(true);

        Player partner = plugin.getMarriageManager().getPartnerPlayer(uuid);
        if (partner == null || !partner.isOnline()) {
            plugin.getChatManager().disableChat(uuid);
            plugin.getLanguageManager().sendMessage(player, "chat-partner-offline");
            return;
        }

        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        plugin.getChatManager().sendCoupleMessage(player, partner, rawMessage);
    }
}
