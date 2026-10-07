package com.harukiyu.claritymarriage.commands;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import com.harukiyu.claritymarriage.models.SharedHome;
import com.harukiyu.claritymarriage.utils.ItemUtil;
import com.harukiyu.claritymarriage.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main command executor for /marry and /marriage.
 */
public class MarryCommand implements CommandExecutor {

    private final ClarityMarriage plugin;
    private final Map<UUID, Long> divorceConfirmations = new ConcurrentHashMap<>();

    public MarryCommand(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getLanguageManager().sendMessage(sender, "only-players");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
            case "tp" -> handleTeleport(player);
            case "sethome" -> handleSetHome(player);
            case "home" -> handleHome(player);
            case "chat" -> handleChat(player, args);
            case "stats" -> handleStats(player, args);
            case "gift" -> handleGift(player);
            case "divorce" -> handleDivorce(player, args);
            case "help" -> sendHelp(player);
            default -> handleProposal(player, args[0]);
        }

        return true;
    }

    private void handleProposal(Player player, String targetName) {
        if (plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            Player partner = plugin.getMarriageManager().getPartnerPlayer(player.getUniqueId());
            String pName = partner != null ? partner.getName() : "your partner";
            plugin.getLanguageManager().sendMessage(player, "already-married", "partner", pName);
            return;
        }

        if (player.getName().equalsIgnoreCase(targetName)) {
            plugin.getLanguageManager().sendMessage(player, "cannot-propose-self");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            plugin.getLanguageManager().sendMessage(player, "player-not-found", "target", targetName);
            return;
        }

        if (plugin.getMarriageManager().isMarried(target.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(player, "target-already-married", "target", target.getName());
            return;
        }

        plugin.getProposalManager().sendProposal(player, target);
    }

    private void handleAccept(Player player) {
        plugin.getProposalManager().acceptProposal(player);
    }

    private void handleDeny(Player player) {
        plugin.getProposalManager().denyProposal(player);
    }

    private void handleTeleport(Player player) {
        if (!player.hasPermission("claritymarriage.tp")) {
            plugin.getLanguageManager().sendMessage(player, "no-permission");
            return;
        }

        if (!plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return;
        }

        plugin.getTeleportManager().requestTeleportToPartner(player);
    }

    private void handleSetHome(Player player) {
        if (!player.hasPermission("claritymarriage.home")) {
            plugin.getLanguageManager().sendMessage(player, "no-permission");
            return;
        }

        if (!plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return;
        }

        plugin.getMarriageManager().setHome(player.getUniqueId(), player.getLocation());
        plugin.getLanguageManager().sendMessage(player, "sethome-success");
    }

    private void handleHome(Player player) {
        if (!player.hasPermission("claritymarriage.home")) {
            plugin.getLanguageManager().sendMessage(player, "no-permission");
            return;
        }

        if (!plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return;
        }

        plugin.getTeleportManager().requestTeleportToHome(player);
    }

    private void handleChat(Player player, String[] args) {
        if (!player.hasPermission("claritymarriage.chat")) {
            plugin.getLanguageManager().sendMessage(player, "no-permission");
            return;
        }

        if (!plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return;
        }

        Player partner = plugin.getMarriageManager().getPartnerPlayer(player.getUniqueId());

        if (args.length > 1) {
            // Direct couple chat message
            if (partner == null || !partner.isOnline()) {
                plugin.getLanguageManager().sendMessage(player, "chat-partner-offline");
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) sb.append(" ");
                sb.append(args[i]);
            }
            plugin.getChatManager().sendCoupleMessage(player, partner, sb.toString());
        } else {
            // Toggle couple chat mode
            plugin.getChatManager().toggleChat(player);
        }
    }

    private void handleStats(Player player, String[] args) {
        if (!player.hasPermission("claritymarriage.stats")) {
            plugin.getLanguageManager().sendMessage(player, "no-permission");
            return;
        }

        UUID targetUuid = player.getUniqueId();
        String targetName = player.getName();

        if (args.length > 1) {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            if (targetPlayer.getName() != null) {
                targetUuid = targetPlayer.getUniqueId();
                targetName = targetPlayer.getName();
            }
        }

        Marriage marriage = plugin.getMarriageManager().getMarriage(targetUuid);
        if (marriage == null) {
            if (targetUuid.equals(player.getUniqueId())) {
                plugin.getLanguageManager().sendMessage(player, "not-married");
            } else {
                plugin.getLanguageManager().sendMessage(player, "target-not-married", "target", targetName);
            }
            return;
        }

        UUID partnerUuid = marriage.getPartner(targetUuid);
        OfflinePlayer partner = Bukkit.getOfflinePlayer(partnerUuid);
        String partnerName = partner.getName() != null ? partner.getName() : "Unknown";
        String partnerStatus = partner.isOnline()
                ? plugin.getLanguageManager().getRaw("stats-partner-online")
                : plugin.getLanguageManager().getRaw("stats-partner-offline");

        String weddingDate = TimeUtil.formatDate(marriage.getMarriageDate());
        String marriedDuration = TimeUtil.formatDuration((System.currentTimeMillis() - marriage.getMarriageDate()) / 1000L);
        String playtime = TimeUtil.formatDuration(marriage.getPlaytimeSeconds());

        SharedHome home = marriage.getSharedHome();
        String homeStatus;
        if (home != null) {
            homeStatus = plugin.getLanguageManager().getRaw("stats-home-set").replace("<world>", home.getWorldName());
        } else {
            homeStatus = plugin.getLanguageManager().getRaw("stats-home-not-set");
        }

        plugin.getLanguageManager().sendRawMessage(player, "stats-card",
                "partner", partnerName,
                "partner_status", partnerStatus,
                "date", weddingDate,
                "married_time", marriedDuration,
                "playtime", playtime,
                "home_status", homeStatus
        );
    }

    private void handleGift(Player player) {
        if (!player.hasPermission("claritymarriage.gift")) {
            plugin.getLanguageManager().sendMessage(player, "no-permission");
            return;
        }

        if (!plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType().isAir() || handItem.getAmount() <= 0) {
            plugin.getLanguageManager().sendMessage(player, "gift-no-item");
            return;
        }

        Player partner = plugin.getMarriageManager().getPartnerPlayer(player.getUniqueId());
        if (partner == null || !partner.isOnline()) {
            plugin.getLanguageManager().sendMessage(player, "gift-partner-offline");
            return;
        }

        String itemName = ItemUtil.getFriendlyName(handItem);
        int itemAmount = handItem.getAmount();

        // Check if partner inventory can fit the item
        if (!ItemUtil.canFitItem(partner, handItem)) {
            plugin.getLanguageManager().sendMessage(player, "gift-inventory-full-sender", "partner", partner.getName());
            plugin.getLanguageManager().sendMessage(partner, "gift-inventory-full-partner",
                    "player", player.getName(),
                    "item", itemName,
                    "amount", String.valueOf(itemAmount)
            );
            return;
        }

        // Safe transfer without duplication or loss
        ItemStack transferStack = handItem.clone();
        player.getInventory().setItemInMainHand(null);

        HashMap<Integer, ItemStack> leftovers = partner.getInventory().addItem(transferStack);
        if (!leftovers.isEmpty()) {
            // Defensive recovery if slots unexpectedly shifted
            for (ItemStack leftover : leftovers.values()) {
                player.getInventory().addItem(leftover);
            }
        }

        player.updateInventory();
        partner.updateInventory();

        // Celebration effects
        if (plugin.getConfigManager().isPlaySounds()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundGiftSuccess(), 1.0f, 1.0f);
            partner.playSound(partner.getLocation(), plugin.getConfigManager().getSoundGiftSuccess(), 1.0f, 1.0f);
        }
        if (plugin.getConfigManager().isSpawnParticles()) {
            partner.spawnParticle(Particle.HEART, partner.getLocation().add(0, 1.2, 0), 12, 0.4, 0.4, 0.4, 0.1);
        }

        plugin.getLanguageManager().sendMessage(player, "gift-sent",
                "partner", partner.getName(),
                "item", itemName,
                "amount", String.valueOf(itemAmount)
        );
        plugin.getLanguageManager().sendMessage(partner, "gift-received",
                "player", player.getName(),
                "item", itemName,
                "amount", String.valueOf(itemAmount)
        );
    }

    private void handleDivorce(Player player, String[] args) {
        if (!plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return;
        }

        UUID pUuid = player.getUniqueId();
        OfflinePlayer partner = plugin.getMarriageManager().getPartnerOfflinePlayer(pUuid);
        String partnerName = partner != null && partner.getName() != null ? partner.getName() : "your partner";

        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            Long confirmationTime = divorceConfirmations.remove(pUuid);
            if (confirmationTime != null && (System.currentTimeMillis() - confirmationTime) <= 15000L) {
                plugin.getMarriageManager().divorce(pUuid);
            } else {
                divorceConfirmations.put(pUuid, System.currentTimeMillis());
                plugin.getLanguageManager().sendRawMessage(player, "divorce-confirm-prompt", "partner", partnerName);
            }
        } else {
            divorceConfirmations.put(pUuid, System.currentTimeMillis());
            plugin.getLanguageManager().sendRawMessage(player, "divorce-confirm-prompt", "partner", partnerName);
        }
    }

    private void sendHelp(Player player) {
        plugin.getLanguageManager().sendRawMessage(player, "help-header");
        plugin.getLanguageManager().sendRawMessage(player, "help-marry");
        plugin.getLanguageManager().sendRawMessage(player, "help-accept");
        plugin.getLanguageManager().sendRawMessage(player, "help-deny");
        plugin.getLanguageManager().sendRawMessage(player, "help-tp");
        plugin.getLanguageManager().sendRawMessage(player, "help-sethome");
        plugin.getLanguageManager().sendRawMessage(player, "help-home");
        plugin.getLanguageManager().sendRawMessage(player, "help-chat");
        plugin.getLanguageManager().sendRawMessage(player, "help-stats");
        plugin.getLanguageManager().sendRawMessage(player, "help-gift");
        plugin.getLanguageManager().sendRawMessage(player, "help-divorce");
        plugin.getLanguageManager().sendRawMessage(player, "help-footer");
    }
}
