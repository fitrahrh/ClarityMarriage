package com.harukiyu.claritymarriage.listeners;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.managers.BankManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Intercepts AsyncChatEvent to handle bank amount prompts and private couple chat channels.
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
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        // 1. Handle bank deposit/withdraw prompts
        if (plugin.getBankManager().isPromptActive(uuid)) {
            event.setCancelled(true);
            BankManager.PromptAction prompt = plugin.getBankManager().getPrompt(uuid);
            plugin.getBankManager().removePrompt(uuid);

            if (rawMessage.equalsIgnoreCase("cancel")) {
                plugin.getLanguageManager().sendMessage(player, "bank-prompt-cancelled");
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getBankGui().open(player));
                return;
            }

            try {
                double amount = Double.parseDouble(rawMessage.replace(",", "").replace("$", ""));
                if (amount <= 0) {
                    plugin.getLanguageManager().sendMessage(player, "bank-prompt-invalid");
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getBankGui().open(player));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (prompt.type() == BankManager.PromptType.DEPOSIT) {
                        plugin.getBankManager().deposit(player, amount);
                    } else if (prompt.type() == BankManager.PromptType.WITHDRAW) {
                        plugin.getBankManager().withdraw(player, amount);
                    }
                    plugin.getBankGui().open(player);
                });
            } catch (NumberFormatException e) {
                plugin.getLanguageManager().sendMessage(player, "bank-prompt-invalid");
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getBankGui().open(player));
            }
            return;
        }

        // 2. Handle private couple chat
        if (!plugin.getChatManager().isChatEnabled(uuid)) {
            return;
        }

        event.setCancelled(true);

        Player partner = plugin.getMarriageManager().getPartnerPlayer(uuid);
        if (partner == null || !partner.isOnline()) {
            plugin.getChatManager().disableChat(uuid);
            plugin.getLanguageManager().sendMessage(player, "chat-partner-offline");
            return;
        }

        plugin.getChatManager().sendCoupleMessage(player, partner, rawMessage);
    }
}
