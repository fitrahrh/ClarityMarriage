package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages couple bank operations, limits, upgrades, and chat prompts.
 */
public class BankManager {

    public enum PromptType {
        DEPOSIT,
        WITHDRAW
    }

    public record PromptAction(PromptType type, long timestamp) {}

    private final ClarityMarriage plugin;
    private final Map<UUID, PromptAction> activePrompts = new ConcurrentHashMap<>();

    public BankManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public boolean isPromptActive(UUID uuid) {
        PromptAction action = activePrompts.get(uuid);
        if (action == null) return false;
        // Expire prompts after 30 seconds
        if (System.currentTimeMillis() - action.timestamp() > 30000L) {
            activePrompts.remove(uuid);
            return false;
        }
        return true;
    }

    public PromptAction getPrompt(UUID uuid) {
        return activePrompts.get(uuid);
    }

    public void setPrompt(UUID uuid, PromptType type) {
        activePrompts.put(uuid, new PromptAction(type, System.currentTimeMillis()));
    }

    public void removePrompt(UUID uuid) {
        activePrompts.remove(uuid);
    }

    /**
     * Deposits money into the couple bank.
     */
    public boolean deposit(Player player, double amount) {
        if (!plugin.getConfigManager().isBankEnabled()) {
            plugin.getLanguageManager().sendMessage(player, "bank-disabled");
            return false;
        }

        if (!plugin.getVaultHook().isAvailable()) {
            plugin.getLanguageManager().sendMessage(player, "bank-no-economy");
            return false;
        }

        if (amount <= 0) return false;

        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (marriage == null) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return false;
        }

        double playerBalance = plugin.getVaultHook().getBalance(player);
        if (playerBalance < amount) {
            plugin.getLanguageManager().sendMessage(player, "bank-not-enough-money",
                    "balance", plugin.getVaultHook().format(playerBalance)
            );
            return false;
        }

        double baseLimit = plugin.getConfigManager().getBankDefaultLimit();
        double perLevel = plugin.getConfigManager().getBankLimitPerLevel();
        double maxLimit = marriage.getBankLimit(baseLimit, perLevel);

        if (marriage.getBankBalance() + amount > maxLimit) {
            plugin.getLanguageManager().sendMessage(player, "bank-limit-reached",
                    "limit", plugin.getVaultHook().format(maxLimit),
                    "current", plugin.getVaultHook().format(marriage.getBankBalance())
            );
            return false;
        }

        // Withdraw from player
        if (!plugin.getVaultHook().withdraw(player, amount)) {
            return false;
        }

        // Add to couple bank
        marriage.addBankBalance(amount, maxLimit);
        plugin.getStorageManager().saveMarriage(marriage);

        String formattedAmount = plugin.getVaultHook().format(amount);
        String formattedBalance = plugin.getVaultHook().format(marriage.getBankBalance());

        plugin.getLanguageManager().sendMessage(player, "bank-deposit-success",
                "amount", formattedAmount,
                "balance", formattedBalance
        );

        Player partner = plugin.getMarriageManager().getPartnerPlayer(player.getUniqueId());
        if (partner != null && partner.isOnline()) {
            plugin.getLanguageManager().sendMessage(partner, "bank-partner-deposit",
                    "partner", player.getName(),
                    "amount", formattedAmount
            );
        }

        return true;
    }

    /**
     * Withdraws money from the couple bank.
     */
    public boolean withdraw(Player player, double amount) {
        if (!plugin.getConfigManager().isBankEnabled()) {
            plugin.getLanguageManager().sendMessage(player, "bank-disabled");
            return false;
        }

        if (!plugin.getVaultHook().isAvailable()) {
            plugin.getLanguageManager().sendMessage(player, "bank-no-economy");
            return false;
        }

        if (amount <= 0) return false;

        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (marriage == null) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return false;
        }

        if (marriage.getBankBalance() < amount) {
            plugin.getLanguageManager().sendMessage(player, "bank-not-enough-bank",
                    "balance", plugin.getVaultHook().format(marriage.getBankBalance())
            );
            return false;
        }

        // Deduct from couple bank
        if (!marriage.withdrawBankBalance(amount)) {
            return false;
        }

        // Deposit to player
        plugin.getVaultHook().deposit(player, amount);
        plugin.getStorageManager().saveMarriage(marriage);

        String formattedAmount = plugin.getVaultHook().format(amount);
        String formattedBalance = plugin.getVaultHook().format(marriage.getBankBalance());

        plugin.getLanguageManager().sendMessage(player, "bank-withdraw-success",
                "amount", formattedAmount,
                "balance", formattedBalance
        );

        Player partner = plugin.getMarriageManager().getPartnerPlayer(player.getUniqueId());
        if (partner != null && partner.isOnline()) {
            plugin.getLanguageManager().sendMessage(partner, "bank-partner-withdraw",
                    "partner", player.getName(),
                    "amount", formattedAmount
            );
        }

        return true;
    }

    /**
     * Upgrades the couple's bank limit using EXP Love.
     */
    public boolean upgradeLimit(Player player) {
        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (marriage == null) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return false;
        }

        int currentLevel = marriage.getBankLevel();
        long cost = plugin.getConfigManager().getUpgradeCost(currentLevel);

        if (marriage.getLoveExp() < cost) {
            plugin.getLanguageManager().sendMessage(player, "bank-not-enough-exp",
                    "level", String.valueOf(currentLevel + 1),
                    "cost", String.valueOf(cost),
                    "current", String.valueOf(marriage.getLoveExp())
            );
            return false;
        }

        // Deduct EXP Love
        if (!marriage.spendLoveExp(cost)) {
            return false;
        }

        // Increment Level
        int newLevel = marriage.incrementBankLevel();
        plugin.getStorageManager().saveMarriage(marriage);

        double baseLimit = plugin.getConfigManager().getBankDefaultLimit();
        double perLevel = plugin.getConfigManager().getBankLimitPerLevel();
        double newLimit = marriage.getBankLimit(baseLimit, perLevel);
        String formattedLimit = plugin.getVaultHook().format(newLimit);

        // Celebration sound & particles
        if (plugin.getConfigManager().isPlaySounds()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundBankUpgrade(), 1.0f, 1.0f);
        }
        if (plugin.getConfigManager().isSpawnParticles()) {
            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 20, 0.5, 0.5, 0.5, 0.1);
        }

        plugin.getLanguageManager().sendRawMessage(player, "bank-upgrade-success",
                "level", String.valueOf(newLevel),
                "limit", formattedLimit
        );

        Player partner = plugin.getMarriageManager().getPartnerPlayer(player.getUniqueId());
        if (partner != null && partner.isOnline()) {
            if (plugin.getConfigManager().isPlaySounds()) {
                partner.playSound(partner.getLocation(), plugin.getConfigManager().getSoundBankUpgrade(), 1.0f, 1.0f);
            }
            plugin.getLanguageManager().sendRawMessage(partner, "bank-upgrade-success",
                    "level", String.valueOf(newLevel),
                    "limit", formattedLimit
            );
        }

        return true;
    }
}
