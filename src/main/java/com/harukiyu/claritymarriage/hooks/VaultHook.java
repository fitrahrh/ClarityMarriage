package com.harukiyu.claritymarriage.hooks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Handles Vault Economy integration for couple bank deposits and withdrawals.
 */
public class VaultHook {

    private Economy economy = null;

    public VaultHook() {}

    public boolean init() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        this.economy = rsp.getProvider();
        return this.economy != null;
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public String getEconomyName() {
        return economy != null ? economy.getName() : "None";
    }

    public double getBalance(Player player) {
        if (economy == null || player == null) return 0.0;
        return economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        if (economy == null || player == null) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (economy == null || player == null || amount <= 0) return false;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    public boolean deposit(Player player, double amount) {
        if (economy == null || player == null || amount <= 0) return false;
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    public String format(double amount) {
        if (economy != null) {
            return economy.format(amount);
        }
        return String.format("$%,.2f", amount);
    }
}
