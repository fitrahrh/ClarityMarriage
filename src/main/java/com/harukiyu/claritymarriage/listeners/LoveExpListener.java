package com.harukiyu.claritymarriage.listeners;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import org.bukkit.Material;
import java.util.UUID;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Listens for game interactions that award EXP Love when married couples play close together.
 */
public class LoveExpListener implements Listener {

    private final ClarityMarriage plugin;

    public LoveExpListener(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;

        Marriage marriage = plugin.getMarriageManager().getMarriage(killer.getUniqueId());
        if (marriage == null) return;

        double maxDist = plugin.getConfigManager().getLoveProximityDistance();
        if (!plugin.getLoveExpManager().isNearPartner(killer, maxDist)) {
            return;
        }

        if (!plugin.getConfigManager().isLoveMobKillEnabled()) {
            return;
        }

        // Check for boss entity
        if (entity instanceof EnderDragon || entity instanceof Wither || entity instanceof Warden || entity instanceof ElderGuardian) {
            int bossExp = plugin.getConfigManager().getLoveBossKillExp();
            plugin.getLoveExpManager().awardLoveExp(marriage, bossExp, "Boss Defeated Together", killer);
            return;
        }

        // Check for standard hostile / hunting mob
        if (entity instanceof Monster) {
            int mobExp = plugin.getConfigManager().getLoveMobKillExp();
            plugin.getLoveExpManager().awardLoveExp(marriage, mobExp, "Hunting Together", killer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isLoveMiningEnabled()) return;

        Player player = event.getPlayer();
        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (marriage == null) return;

        double maxDist = plugin.getConfigManager().getLoveProximityDistance();
        if (!plugin.getLoveExpManager().isNearPartner(player, maxDist)) {
            return;
        }

        Material type = event.getBlock().getType();
        if (isValuableOre(type)) {
            int exp = plugin.getConfigManager().getLoveMiningExp();
            plugin.getLoveExpManager().awardLoveExp(marriage, exp, "Mining Together", player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!plugin.getConfigManager().isLoveFishingEnabled()) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH && event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }

        Player player = event.getPlayer();
        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (marriage == null) return;

        double maxDist = plugin.getConfigManager().getLoveProximityDistance();
        if (plugin.getLoveExpManager().isNearPartner(player, maxDist)) {
            int exp = plugin.getConfigManager().getLoveFishingExp();
            plugin.getLoveExpManager().awardLoveExp(marriage, exp, "Fishing Together", player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!plugin.getConfigManager().isLoveSleepingEnabled()) return;
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;

        Player player = event.getPlayer();
        Player partner = plugin.getMarriageManager().getPartnerPlayer(player.getUniqueId());
        if (partner == null || !partner.isOnline()) return;

        Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
        if (marriage == null) return;

        // Check if partner is sleeping or nearby in bed
        if (partner.isSleeping() || plugin.getLoveExpManager().isNearPartner(player, 4.0)) {
            int exp = plugin.getConfigManager().getLoveSleepingExp();
            plugin.getLoveExpManager().awardLoveExp(marriage, exp, "Sweet Dreams Together", player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractPartner(PlayerInteractEntityEvent event) {
        if (!plugin.getConfigManager().isLoveKissEnabled()) return;
        Player player = event.getPlayer();

        if (!player.isSneaking() || !(event.getRightClicked() instanceof Player clicked)) {
            return;
        }

        UUID partnerUuid = plugin.getMarriageManager().getPartnerUuid(player.getUniqueId());
        if (partnerUuid != null && partnerUuid.equals(clicked.getUniqueId())) {
            event.setCancelled(true);
            plugin.getLoveExpManager().handleKiss(player, clicked);
        }
    }

    private boolean isValuableOre(Material mat) {
        return mat == Material.DIAMOND_ORE || mat == Material.DEEPSLATE_DIAMOND_ORE
                || mat == Material.ANCIENT_DEBRIS
                || mat == Material.EMERALD_ORE || mat == Material.DEEPSLATE_EMERALD_ORE
                || mat == Material.GOLD_ORE || mat == Material.DEEPSLATE_GOLD_ORE
                || mat == Material.NETHER_GOLD_ORE
                || mat == Material.IRON_ORE || mat == Material.DEEPSLATE_IRON_ORE;
    }
}
