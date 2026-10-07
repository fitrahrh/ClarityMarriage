package com.harukiyu.claritymarriage.commands;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Gender;
import com.harukiyu.claritymarriage.models.Marriage;
import com.harukiyu.claritymarriage.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Admin command executor and tab completer for /claritymarriage and /claritymarry.
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final ClarityMarriage plugin;

    public AdminCommand(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("claritymarriage.admin")) {
            plugin.getLanguageManager().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendAdminHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "reload" -> handleReload(sender);
            case "forcemarry" -> handleForceMarry(sender, args);
            case "forcedivorce" -> handleForceDivorce(sender, args);
            case "cleardivorce" -> handleClearDivorce(sender, args);
            case "setgender" -> handleSetGender(sender, args);
            case "info" -> handleInfo(sender, args);
            default -> sendAdminHelp(sender);
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().load();
        plugin.getLanguageManager().load();
        plugin.getBankGui().load();
        plugin.getLanguageManager().sendMessage(sender, "admin-reload");
    }

    private void handleForceMarry(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /claritymarriage forcemarry <player1> <player2>");
            return;
        }

        Player p1 = Bukkit.getPlayerExact(args[1]);
        Player p2 = Bukkit.getPlayerExact(args[2]);

        if (p1 == null || p2 == null) {
            plugin.getLanguageManager().sendMessage(sender, "player-not-found", "target", p1 == null ? args[1] : args[2]);
            return;
        }

        if (p1.getUniqueId().equals(p2.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(sender, "cannot-propose-self");
            return;
        }

        if (plugin.getMarriageManager().isMarried(p1.getUniqueId())) {
            plugin.getMarriageManager().divorce(p1.getUniqueId());
        }
        if (plugin.getMarriageManager().isMarried(p2.getUniqueId())) {
            plugin.getMarriageManager().divorce(p2.getUniqueId());
        }

        plugin.getMarriageManager().createMarriage(p1.getUniqueId(), p2.getUniqueId());
        plugin.getLanguageManager().sendMessage(sender, "admin-force-marry",
                "player1", p1.getName(),
                "player2", p2.getName()
        );
    }

    private void handleForceDivorce(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /claritymarriage forcedivorce <player>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null || !plugin.getMarriageManager().isMarried(target.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(sender, "target-not-married", "target", args[1]);
            return;
        }

        UUID partnerUuid = plugin.getMarriageManager().getPartnerUuid(target.getUniqueId());
        OfflinePlayer partner = Bukkit.getOfflinePlayer(partnerUuid);

        plugin.getMarriageManager().divorce(target.getUniqueId());
        plugin.getLanguageManager().sendMessage(sender, "admin-force-divorce",
                "player1", target.getName(),
                "player2", partner.getName() != null ? partner.getName() : "Partner"
        );
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /claritymarriage info <player>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Marriage marriage = plugin.getMarriageManager().getMarriage(target.getUniqueId());
        if (marriage == null) {
            plugin.getLanguageManager().sendMessage(sender, "target-not-married", "target", args[1]);
            return;
        }

        UUID partnerUuid = marriage.getPartner(target.getUniqueId());
        OfflinePlayer partner = Bukkit.getOfflinePlayer(partnerUuid);

        String weddingDate = TimeUtil.formatDate(marriage.getMarriageDate());
        String playtime = TimeUtil.formatDuration(marriage.getPlaytimeSeconds());

        plugin.getLanguageManager().sendRawMessage(sender, "admin-info",
                "player", target.getName() != null ? target.getName() : args[1],
                "partner", partner.getName() != null ? partner.getName() : "Unknown",
                "date", weddingDate,
                "playtime", playtime
        );
    }

    private void handleClearDivorce(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /claritymarriage cleardivorce <player>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null) {
            plugin.getLanguageManager().sendMessage(sender, "player-not-found", "target", args[1]);
            return;
        }

        plugin.getProfileManager().clearDivorceCooldown(target.getUniqueId());
        sender.sendMessage("§aDivorce cooldown cleared for §e" + target.getName() + "§a.");
    }

    private void handleSetGender(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /claritymarriage setgender <player> <male/female/none>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null) {
            plugin.getLanguageManager().sendMessage(sender, "player-not-found", "target", args[1]);
            return;
        }

        Gender gender = Gender.fromString(args[2]);
        plugin.getProfileManager().setGender(target.getUniqueId(), gender);
        sender.sendMessage("§aGender for §e" + target.getName() + "§a set to §e" + gender.getDisplayName() + " " + gender.getIcon() + "§a.");
    }

    private void sendAdminHelp(CommandSender sender) {
        plugin.getLanguageManager().sendRawMessage(sender, "admin-help-header");
        plugin.getLanguageManager().sendRawMessage(sender, "admin-help-reload");
        plugin.getLanguageManager().sendRawMessage(sender, "admin-help-marry");
        plugin.getLanguageManager().sendRawMessage(sender, "admin-help-divorce");
        plugin.getLanguageManager().sendRawMessage(sender, "admin-help-info");
        sender.sendMessage("§e/cmarry cleardivorce <player> §7- §fReset player's divorce cooldown");
        sender.sendMessage("§e/cmarry setgender <player> <gender> §7- §fSet player's gender (male/female/none)");
        plugin.getLanguageManager().sendRawMessage(sender, "admin-help-footer");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("claritymarriage.admin")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String current = args[0].toLowerCase(Locale.ROOT);
            for (String sub : List.of("reload", "forcemarry", "forcedivorce", "cleardivorce", "setgender", "info", "help")) {
                if (sub.startsWith(current)) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String current = args[1].toLowerCase(Locale.ROOT);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(current)) {
                    completions.add(p.getName());
                }
            }
            return completions;
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            String current = args[2].toLowerCase(Locale.ROOT);
            if ("setgender".equals(sub)) {
                for (String g : List.of("male", "female", "none")) {
                    if (g.startsWith(current)) {
                        completions.add(g);
                    }
                }
                return completions;
            } else if ("forcemarry".equals(sub)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(current)) {
                        completions.add(p.getName());
                    }
                }
                return completions;
            }
        }

        return Collections.emptyList();
    }
}
