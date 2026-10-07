package com.harukiyu.claritymarriage.hooks;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import com.harukiyu.claritymarriage.utils.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * PlaceholderAPI expansion for ClarityMarriage.
 */
public class MarriagePlaceholderExpansion extends PlaceholderExpansion {

    private final ClarityMarriage plugin;

    public MarriagePlaceholderExpansion(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "claritymarriage";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Harukiyu";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        UUID uuid = player.getUniqueId();
        boolean isMarried = plugin.getMarriageManager().isMarried(uuid);
        Marriage marriage = plugin.getMarriageManager().getMarriage(uuid);

        return switch (params.toLowerCase()) {
            case "is_married" -> isMarried ? "yes" : "no";
            case "status_icon" -> isMarried ? "❤" : "";
            case "partner" -> {
                if (!isMarried) yield "";
                UUID partnerUuid = marriage.getPartner(uuid);
                OfflinePlayer partner = Bukkit.getOfflinePlayer(partnerUuid);
                yield partner.getName() != null ? partner.getName() : "";
            }
            case "partner_online" -> {
                if (!isMarried) yield "";
                UUID partnerUuid = marriage.getPartner(uuid);
                Player partner = Bukkit.getPlayer(partnerUuid);
                yield (partner != null && partner.isOnline()) ? "Online" : "Offline";
            }
            case "wedding_date" -> {
                if (!isMarried) yield "";
                yield TimeUtil.formatDate(marriage.getMarriageDate());
            }
            case "married_duration" -> {
                if (!isMarried) yield "";
                yield TimeUtil.formatDuration((System.currentTimeMillis() - marriage.getMarriageDate()) / 1000L);
            }
            case "playtime_together" -> {
                if (!isMarried) yield "0m";
                yield TimeUtil.formatDuration(marriage.getPlaytimeSeconds());
            }
            case "has_home" -> {
                if (!isMarried) yield "no";
                yield marriage.getSharedHome() != null ? "yes" : "no";
            }
            default -> null;
        };
    }
}
