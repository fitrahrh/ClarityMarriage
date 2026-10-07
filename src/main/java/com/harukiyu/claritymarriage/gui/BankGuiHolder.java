package com.harukiyu.claritymarriage.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Custom InventoryHolder to cleanly identify ClarityMarriage Bank GUIs.
 */
public class BankGuiHolder implements InventoryHolder {

    private final UUID playerUuid;
    private Inventory inventory;

    public BankGuiHolder(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            inventory = org.bukkit.Bukkit.createInventory(this, 45);
        }
        return inventory;
    }
}
