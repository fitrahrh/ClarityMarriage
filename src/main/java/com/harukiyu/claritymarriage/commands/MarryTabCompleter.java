package com.harukiyu.claritymarriage.commands;

import com.harukiyu.claritymarriage.ClarityMarriage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Tab completer for /marry command providing context-sensitive suggestions.
 */
public class MarryTabCompleter implements TabCompleter {

    private final ClarityMarriage plugin;

    public MarryTabCompleter(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String current = args[0].toLowerCase(Locale.ROOT);
            boolean isMarried = plugin.getMarriageManager().isMarried(player.getUniqueId());

            List<String> options = new ArrayList<>();
            if (isMarried) {
                options.add("tp");
                options.add("sethome");
                options.add("home");
                options.add("chat");
                options.add("stats");
                options.add("gift");
                options.add("divorce");
                options.add("help");
            } else {
                options.add("accept");
                options.add("deny");
                options.add("stats");
                options.add("help");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getUniqueId().equals(player.getUniqueId())) {
                        options.add(p.getName());
                    }
                }
            }

            for (String opt : options) {
                if (opt.toLowerCase(Locale.ROOT).startsWith(current)) {
                    completions.add(opt);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            String current = args[1].toLowerCase(Locale.ROOT);

            if ("divorce".equals(sub)) {
                if ("confirm".startsWith(current)) {
                    completions.add("confirm");
                }
            } else if ("stats".equals(sub)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(current)) {
                        completions.add(p.getName());
                    }
                }
            }
            return completions;
        }

        return Collections.emptyList();
    }
}
