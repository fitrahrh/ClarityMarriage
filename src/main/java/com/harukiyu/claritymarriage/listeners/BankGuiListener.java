package com.harukiyu.claritymarriage.listeners;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.gui.BankAction;
import com.harukiyu.claritymarriage.gui.BankGuiHolder;
import com.harukiyu.claritymarriage.managers.BankManager;
import com.harukiyu.claritymarriage.models.Marriage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Listens for interactions in the Bank GUI, preventing item movement and executing transactions.
 */
public class BankGuiListener implements Listener {

    private final ClarityMarriage plugin;

    public BankGuiListener(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BankGuiHolder)) {
            return;
        }

        // Cancel all clicks in the bank GUI to prevent item theft/movement
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        org.bukkit.inventory.Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null || !(clickedInv.getHolder() instanceof BankGuiHolder)) {
            return;
        }

        int slot = event.getSlot();
        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (marriage == null) {
            player.closeInventory();
            return;
        }

        BankAction action = plugin.getBankGui().getActionAtSlot(slot);
        if (action == null) {
            return;
        }

        boolean updated = false;

        switch (action) {
            case DEPOSIT_1, DEPOSIT_2, DEPOSIT_3 -> {
                double amount = plugin.getBankGui().getAmount(action);
                plugin.getBankManager().deposit(player, amount);
                updated = true;
            }
            case DEPOSIT_ALL -> {
                double baseLimit = plugin.getConfigManager().getBankDefaultLimit();
                double perLevel = plugin.getConfigManager().getBankLimitPerLevel();
                double maxLimit = marriage.getBankLimit(baseLimit, perLevel);
                double space = maxLimit - marriage.getBankBalance();
                double wallet = plugin.getVaultHook().getBalance(player);
                double toDeposit = Math.min(space, wallet);
                if (toDeposit > 0) {
                    plugin.getBankManager().deposit(player, toDeposit);
                    updated = true;
                } else if (space <= 0) {
                    plugin.getLanguageManager().sendMessage(player, "bank-limit-reached",
                            "limit", plugin.getVaultHook().format(maxLimit),
                            "current", plugin.getVaultHook().format(marriage.getBankBalance())
                    );
                } else {
                    plugin.getLanguageManager().sendMessage(player, "bank-not-enough-money",
                            "balance", plugin.getVaultHook().format(wallet)
                    );
                }
            }
            case DEPOSIT_CUSTOM -> {
                player.closeInventory();
                plugin.getBankManager().setPrompt(player.getUniqueId(), BankManager.PromptType.DEPOSIT);
                plugin.getLanguageManager().sendRawMessage(player, "bank-prompt-deposit");
            }
            case WITHDRAW_1, WITHDRAW_2, WITHDRAW_3 -> {
                double amount = plugin.getBankGui().getAmount(action);
                plugin.getBankManager().withdraw(player, amount);
                updated = true;
            }
            case WITHDRAW_ALL -> {
                double vaultBal = marriage.getBankBalance();
                if (vaultBal > 0) {
                    plugin.getBankManager().withdraw(player, vaultBal);
                    updated = true;
                } else {
                    plugin.getLanguageManager().sendMessage(player, "bank-not-enough-bank",
                            "balance", plugin.getVaultHook().format(0.0)
                    );
                }
            }
            case WITHDRAW_CUSTOM -> {
                player.closeInventory();
                plugin.getBankManager().setPrompt(player.getUniqueId(), BankManager.PromptType.WITHDRAW);
                plugin.getLanguageManager().sendRawMessage(player, "bank-prompt-withdraw");
            }
            case UPGRADE_LIMIT -> {
                if (player.hasPermission("claritymarriage.bank.upgrade")) {
                    plugin.getBankManager().upgradeLimit(player);
                    updated = true;
                } else {
                    plugin.getLanguageManager().sendMessage(player, "no-permission");
                }
            }
            case LOVE_TIPS -> {
                player.closeInventory();
                sendLoveGuide(player, marriage);
            }
            case CLOSE -> player.closeInventory();
            case OVERVIEW -> {}
        }

        org.bukkit.inventory.Inventory topInv = player.getOpenInventory().getTopInventory();
        if (updated && topInv.getHolder() instanceof BankGuiHolder) {
            plugin.getBankGui().refresh(player, topInv);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BankGuiHolder) {
            event.setCancelled(true);
        }
    }

    private void sendLoveGuide(Player player, Marriage marriage) {
        double baseLimit = plugin.getConfigManager().getBankDefaultLimit();
        double perLevel = plugin.getConfigManager().getBankLimitPerLevel();
        double limit = marriage.getBankLimit(baseLimit, perLevel);

        plugin.getLanguageManager().sendRawMessage(player, "love-guide",
                "love_exp", String.valueOf(marriage.getLoveExp()),
                "bank_level", String.valueOf(marriage.getBankLevel()),
                "bank_limit", plugin.getVaultHook().format(limit)
        );
    }
}
