package com.harukiyu.claritymarriage.gui;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.*;

/**
 * Modern, fully configurable chest GUI for depositing, withdrawing, and upgrading the couple bank limit.
 * All slots, materials, titles, and lore are loaded from bankgui.yml.
 */
public class BankGui {

    private final ClarityMarriage plugin;
    private final MiniMessage mm;
    private final File configFile;

    private String guiTitle;
    private int guiSize;
    private boolean borderEnabled;
    private Material borderMaterial;
    private String borderName;
    private List<Integer> borderSlots;

    private final Map<Integer, BankAction> slotToActions = new HashMap<>();
    private final Map<BankAction, GuiItemConfig> actionConfigs = new EnumMap<>(BankAction.class);

    public static class GuiItemConfig {
        public int slot;
        public Material material;
        public String name;
        public List<String> lore = new ArrayList<>();
        public double amount;
        public String statusReady;
        public String statusNotEnough;
    }

    public BankGui(ClarityMarriage plugin) {
        this.plugin = plugin;
        this.mm = plugin.getLanguageManager().getMiniMessage();
        this.configFile = new File(plugin.getDataFolder(), "bankgui.yml");
    }

    public void load() {
        if (!configFile.exists()) {
            plugin.saveResource("bankgui.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        guiTitle = config.getString("title", "<gradient:#ff758c:#ff7eb3><b>❤ Couple Bank & Vault ❤</b></gradient>");
        int rawSize = config.getInt("size", 45);
        if (rawSize % 9 != 0 || rawSize < 9 || rawSize > 54) {
            guiSize = 45;
        } else {
            guiSize = rawSize;
        }

        borderEnabled = config.getBoolean("border.enabled", true);
        borderMaterial = parseMaterial(config.getString("border.material", "MAGENTA_STAINED_GLASS_PANE"), Material.MAGENTA_STAINED_GLASS_PANE);
        borderName = config.getString("border.name", "<gray> </gray>");
        borderSlots = config.getIntegerList("border.slots");

        slotToActions.clear();
        actionConfigs.clear();

        loadItem(config, "overview", BankAction.OVERVIEW, 4, Material.PLAYER_HEAD,
                "<gradient:#ff758c:#ff7eb3><b>Couple Vault Overview</b></gradient>", 0.0);
        loadItem(config, "deposit-1", BankAction.DEPOSIT_1, 10, Material.EMERALD,
                "<green><b>Deposit $10,000</b></green>", 10000.0);
        loadItem(config, "deposit-2", BankAction.DEPOSIT_2, 11, Material.EMERALD,
                "<green><b>Deposit $50,000</b></green>", 50000.0);
        loadItem(config, "deposit-3", BankAction.DEPOSIT_3, 12, Material.EMERALD,
                "<green><b>Deposit $100,000</b></green>", 100000.0);
        loadItem(config, "deposit-all", BankAction.DEPOSIT_ALL, 13, Material.GOLD_BLOCK,
                "<gold><b>Deposit All / Max</b></gold>", 0.0);
        loadItem(config, "deposit-custom", BankAction.DEPOSIT_CUSTOM, 14, Material.PAPER,
                "<yellow><b>Deposit Custom Amount</b></yellow>", 0.0);

        loadItem(config, "withdraw-1", BankAction.WITHDRAW_1, 19, Material.REDSTONE,
                "<red><b>Withdraw $10,000</b></red>", 10000.0);
        loadItem(config, "withdraw-2", BankAction.WITHDRAW_2, 20, Material.REDSTONE,
                "<red><b>Withdraw $50,000</b></red>", 50000.0);
        loadItem(config, "withdraw-3", BankAction.WITHDRAW_3, 21, Material.REDSTONE,
                "<red><b>Withdraw $100,000</b></red>", 100000.0);
        loadItem(config, "withdraw-all", BankAction.WITHDRAW_ALL, 22, Material.REDSTONE_BLOCK,
                "<dark_red><b>Withdraw All</b></dark_red>", 0.0);
        loadItem(config, "withdraw-custom", BankAction.WITHDRAW_CUSTOM, 23, Material.PAPER,
                "<yellow><b>Withdraw Custom Amount</b></yellow>", 0.0);

        loadItem(config, "upgrade-limit", BankAction.UPGRADE_LIMIT, 31, Material.NETHER_STAR,
                "<gradient:#ff758c:#ff7eb3><b>⭐ UPGRADE BANK LIMIT ⭐</b></gradient>", 0.0);
        loadItem(config, "love-tips", BankAction.LOVE_TIPS, 33, Material.BOOK,
                "<gold><b>📖 How to Earn EXP Love?</b></gold>", 0.0);
        loadItem(config, "close", BankAction.CLOSE, 40, Material.BARRIER,
                "<red><b>Close</b></red>", 0.0);
    }

    private void loadItem(YamlConfiguration config, String key, BankAction action, int defaultSlot,
                          Material defaultMat, String defaultName, double defaultAmount) {
        String path = "items." + key;
        GuiItemConfig item = new GuiItemConfig();
        item.slot = config.getInt(path + ".slot", defaultSlot);
        item.material = parseMaterial(config.getString(path + ".material", defaultMat.name()), defaultMat);
        item.name = config.getString(path + ".name", defaultName);
        item.lore = config.getStringList(path + ".lore");
        item.amount = config.getDouble(path + ".amount", defaultAmount);

        if (action == BankAction.UPGRADE_LIMIT) {
            item.statusReady = config.getString(path + ".status-ready", "<green>✔ Click to Upgrade Bank Limit!</green>");
            item.statusNotEnough = config.getString(path + ".status-not-enough", "<red>✘ Not enough EXP Love! (Earn with partner)</red>");
        }

        actionConfigs.put(action, item);
        if (item.slot >= 0 && item.slot < guiSize) {
            slotToActions.put(item.slot, action);
        }
    }

    private Material parseMaterial(String name, Material fallback) {
        if (name == null || name.isEmpty()) return fallback;
        try {
            Material mat = Material.matchMaterial(name.toUpperCase(Locale.ROOT));
            return mat != null ? mat : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    public BankAction getActionAtSlot(int slot) {
        return slotToActions.get(slot);
    }

    public double getAmount(BankAction action) {
        GuiItemConfig cfg = actionConfigs.get(action);
        return cfg != null ? cfg.amount : 0.0;
    }

    public void open(Player player) {
        UUID uuid = player.getUniqueId();
        Marriage marriage = plugin.getMarriageManager().getMarriage(uuid);
        if (marriage == null) {
            plugin.getLanguageManager().sendMessage(player, "not-married");
            return;
        }

        BankGuiHolder holder = new BankGuiHolder(uuid);
        Component title = mm.deserialize(guiTitle != null ? guiTitle : "<gradient:#ff758c:#ff7eb3><b>❤ Couple Bank & Vault ❤</b></gradient>");
        Inventory inv = Bukkit.createInventory(holder, guiSize, title);
        holder.setInventory(inv);

        populate(inv, player, marriage);
        player.openInventory(inv);
    }

    public void refresh(Player player, Inventory inv) {
        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (marriage == null) {
            player.closeInventory();
            return;
        }
        inv.clear();
        populate(inv, player, marriage);
    }

    private void populate(Inventory inv, Player player, Marriage marriage) {
        // 1. Fill borders
        if (borderEnabled) {
            ItemStack border = createItem(borderMaterial, borderName);
            if (borderSlots != null && !borderSlots.isEmpty()) {
                for (int slot : borderSlots) {
                    if (slot >= 0 && slot < inv.getSize()) {
                        inv.setItem(slot, border);
                    }
                }
            } else {
                for (int i = 0; i < inv.getSize(); i++) {
                    if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || (i + 1) % 9 == 0) {
                        inv.setItem(i, border);
                    }
                }
            }
        }

        // 2. Compute dynamic stats
        UUID partnerUuid = marriage.getPartner(player.getUniqueId());
        OfflinePlayer partner = Bukkit.getOfflinePlayer(partnerUuid);
        String partnerName = partner.getName() != null ? partner.getName() : "Partner";

        double balance = marriage.getBankBalance();
        double baseLimit = plugin.getConfigManager().getBankDefaultLimit();
        double perLevel = plugin.getConfigManager().getBankLimitPerLevel();
        double maxLimit = marriage.getBankLimit(baseLimit, perLevel);
        int currentLevel = marriage.getBankLevel();
        long loveExp = marriage.getLoveExp();

        String fmtBalance = plugin.getVaultHook().format(balance);
        String fmtLimit = plugin.getVaultHook().format(maxLimit);
        String fmtWallet = plugin.getVaultHook().format(plugin.getVaultHook().getBalance(player));

        long cost = plugin.getConfigManager().getUpgradeCost(currentLevel);
        double nextLimit = maxLimit + perLevel;
        String fmtNextLimit = plugin.getVaultHook().format(nextLimit);
        boolean hasEnough = loveExp >= cost;

        // 3. Render each configured action item
        for (Map.Entry<BankAction, GuiItemConfig> entry : actionConfigs.entrySet()) {
            BankAction action = entry.getKey();
            GuiItemConfig cfg = entry.getValue();
            if (cfg.slot < 0 || cfg.slot >= inv.getSize()) continue;

            String resolvedName = replacePlaceholders(cfg.name, partnerName, fmtBalance, fmtLimit, currentLevel,
                    fmtWallet, loveExp, fmtNextLimit, currentLevel + 1, cost, hasEnough, cfg);
            List<String> resolvedLore = new ArrayList<>();
            for (String l : cfg.lore) {
                resolvedLore.add(replacePlaceholders(l, partnerName, fmtBalance, fmtLimit, currentLevel,
                        fmtWallet, loveExp, fmtNextLimit, currentLevel + 1, cost, hasEnough, cfg));
            }

            ItemStack item;
            if (action == BankAction.OVERVIEW && cfg.material == Material.PLAYER_HEAD) {
                item = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                if (skull != null) {
                    skull.setOwningPlayer(player);
                    skull.displayName(mm.deserialize(resolvedName));
                    List<Component> loreComponents = new ArrayList<>();
                    for (String l : resolvedLore) {
                        loreComponents.add(mm.deserialize(l));
                    }
                    skull.lore(loreComponents);
                    item.setItemMeta(skull);
                }
            } else {
                item = createItem(cfg.material, resolvedName, resolvedLore);
            }

            inv.setItem(cfg.slot, item);
        }
    }

    private String replacePlaceholders(String text, String partner, String balance, String limit, int level,
                                       String wallet, long loveExp, String nextLimit, int nextLevel, long cost,
                                       boolean hasEnough, GuiItemConfig cfg) {
        if (text == null) return "";
        String statusMsg = hasEnough
                ? (cfg.statusReady != null ? cfg.statusReady : "<green>✔ Click to Upgrade Bank Limit!</green>")
                : (cfg.statusNotEnough != null ? cfg.statusNotEnough : "<red>✘ Not enough EXP Love! (Earn with partner)</red>");
        String expColor = hasEnough ? "<green>" : "<red>";

        return text.replace("<partner>", partner)
                .replace("<balance>", balance)
                .replace("<limit>", limit)
                .replace("<level>", String.valueOf(level))
                .replace("<wallet>", wallet)
                .replace("<love_exp>", String.valueOf(loveExp))
                .replace("<next_limit>", nextLimit)
                .replace("<next_level>", String.valueOf(nextLevel))
                .replace("<cost>", String.valueOf(cost))
                .replace("<love_exp_color>", expColor)
                .replace("<status_message>", statusMsg);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreList = new ArrayList<>();
                for (String l : lore) {
                    loreList.add(mm.deserialize(l));
                }
                meta.lore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
