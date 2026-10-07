package com.harukiyu.claritymarriage.hooks;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Gender;
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
        Gender playerGender = plugin.getProfileManager() != null ? plugin.getProfileManager().getGender(uuid) : Gender.NONE;

        return switch (params.toLowerCase()) {
            case "is_married" -> isMarried ? "yes" : "no";
            case "status" -> isMarried ? "Married" : "Single";
            case "status_icon" -> isMarried ? "❤" : "";
            case "gender" -> playerGender != Gender.NONE ? playerGender.getDisplayName() : "None";
            case "gender_icon" -> playerGender.getIcon();
            case "divorce_cooldown" -> {
                if (plugin.getProfileManager() != null && plugin.getProfileManager().isInDivorceCooldown(uuid)) {
                    yield TimeUtil.formatDuration(plugin.getProfileManager().getRemainingDivorceCooldownSeconds(uuid));
                }
                yield "0s";
            }
            case "divorce_cooldown_seconds" -> {
                if (plugin.getProfileManager() != null && plugin.getProfileManager().isInDivorceCooldown(uuid)) {
                    yield String.valueOf(plugin.getProfileManager().getRemainingDivorceCooldownSeconds(uuid));
                }
                yield "0";
            }
            case "partner" -> {
                if (!isMarried) yield "";
                UUID partnerUuid = marriage.getPartner(uuid);
                OfflinePlayer partner = Bukkit.getOfflinePlayer(partnerUuid);
                yield partner.getName() != null ? partner.getName() : "";
            }
            case "partner_gender" -> {
                if (!isMarried) yield "";
                UUID partnerUuid = marriage.getPartner(uuid);
                Gender pg = plugin.getProfileManager() != null ? plugin.getProfileManager().getGender(partnerUuid) : Gender.NONE;
                yield pg != Gender.NONE ? pg.getDisplayName() : "";
            }
            case "partner_gender_icon" -> {
                if (!isMarried) yield "";
                UUID partnerUuid = marriage.getPartner(uuid);
                Gender pg = plugin.getProfileManager() != null ? plugin.getProfileManager().getGender(partnerUuid) : Gender.NONE;
                yield pg.getIcon();
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
            case "bank_balance" -> {
                if (!isMarried) yield "0";
                yield String.valueOf(marriage.getBankBalance());
            }
            case "bank_balance_formatted" -> {
                if (!isMarried) yield "$0.00";
                yield plugin.getVaultHook().format(marriage.getBankBalance());
            }
            case "bank_limit" -> {
                if (!isMarried) yield "0";
                double base = plugin.getConfigManager().getBankDefaultLimit();
                double per = plugin.getConfigManager().getBankLimitPerLevel();
                yield String.valueOf(marriage.getBankLimit(base, per));
            }
            case "bank_limit_formatted" -> {
                if (!isMarried) yield "$0.00";
                double base = plugin.getConfigManager().getBankDefaultLimit();
                double per = plugin.getConfigManager().getBankLimitPerLevel();
                yield plugin.getVaultHook().format(marriage.getBankLimit(base, per));
            }
            case "bank_level" -> {
                if (!isMarried) yield "1";
                yield String.valueOf(marriage.getBankLevel());
            }
            case "love_exp" -> {
                if (!isMarried) yield "0";
                yield String.valueOf(marriage.getLoveExp());
            }
            default -> null;
        };
    }
}
