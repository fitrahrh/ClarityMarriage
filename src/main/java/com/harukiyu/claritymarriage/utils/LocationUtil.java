package com.harukiyu.claritymarriage.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Utility class for location validation and teleport safety checks.
 */
public final class LocationUtil {

    private LocationUtil() {}

    /**
     * Checks if the destination location is safe for a player to teleport to.
     */
    public static boolean isSafeLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }

        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if (y < world.getMinHeight() || y >= world.getMaxHeight() - 1) {
            return false;
        }

        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Block ground = world.getBlockAt(x, y - 1, z);

        // Feet and head blocks must be passable
        if (isHazardous(feet.getType()) || isHazardous(head.getType())) {
            return false;
        }

        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }

        // Ground must be solid and not hazardous
        if (isHazardous(ground.getType())) {
            return false;
        }

        return ground.getType().isSolid();
    }

    private static boolean isHazardous(Material mat) {
        return mat == Material.LAVA
                || mat == Material.FIRE
                || mat == Material.SOUL_FIRE
                || mat == Material.CAMPFIRE
                || mat == Material.SOUL_CAMPFIRE
                || mat == Material.WITHER_ROSE
                || mat == Material.CACTUS
                || mat == Material.SWEET_BERRY_BUSH;
    }
}
