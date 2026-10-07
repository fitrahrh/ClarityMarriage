package com.harukiyu.claritymarriage.utils;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

/**
 * Utility class for item manipulation, names, and safe inventory space calculation.
 */
public final class ItemUtil {

    private ItemUtil() {}

    /**
     * Determines whether the player's storage inventory can fit the entire item stack.
     */
    public static boolean canFitItem(Player player, ItemStack item) {
        if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return false;
        }

        int needed = item.getAmount();
        int maxStack = item.getMaxStackSize();
        ItemStack[] storage = player.getInventory().getStorageContents();
        if (storage == null) {
            return false;
        }

        for (ItemStack slot : storage) {
            if (slot == null || slot.getType().isAir()) {
                needed -= maxStack;
            } else if (slot.isSimilar(item)) {
                int room = maxStack - slot.getAmount();
                if (room > 0) {
                    needed -= room;
                }
            }

            if (needed <= 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets a clean, formatted display name for the given item.
     */
    public static String getFriendlyName(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Air";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            net.kyori.adventure.text.Component dName = meta.displayName();
            if (dName != null) {
                return PlainTextComponentSerializer.plainText().serialize(dName);
            }
        }

        return formatMaterialName(item.getType());
    }

    /**
     * Converts Material enum name to Title Case (e.g., GOLDEN_APPLE -> Golden Apple).
     */
    public static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : name.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalize = true;
                sb.append(c);
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
