package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Proposal;
import com.harukiyu.claritymarriage.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active marriage proposals, cooldowns, and expiration tracking.
 */
public class ProposalManager {

    private final ClarityMarriage plugin;
    private final Map<UUID, Proposal> proposalsByReceiver = new ConcurrentHashMap<>();
    private final Map<UUID, Long> proposalCooldowns = new ConcurrentHashMap<>();
    private BukkitTask cleanupTask;

    public ProposalManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpired, 20L, 20L);
    }

    public void stop() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        proposalsByReceiver.clear();
        proposalCooldowns.clear();
    }

    public boolean sendProposal(Player sender, Player receiver) {
        UUID sUuid = sender.getUniqueId();
        UUID rUuid = receiver.getUniqueId();

        // Check divorce cooldown for sender
        if (plugin.getProfileManager().isInDivorceCooldown(sUuid)) {
            long remaining = plugin.getProfileManager().getRemainingDivorceCooldownSeconds(sUuid);
            plugin.getLanguageManager().sendMessage(sender, "divorce-cooldown-active", "time", TimeUtil.formatDuration(remaining));
            return false;
        }

        // Check divorce cooldown for receiver
        if (plugin.getProfileManager().isInDivorceCooldown(rUuid)) {
            long remaining = plugin.getProfileManager().getRemainingDivorceCooldownSeconds(rUuid);
            plugin.getLanguageManager().sendMessage(sender, "target-divorce-cooldown", "target", receiver.getName(), "time", TimeUtil.formatDuration(remaining));
            return false;
        }

        // Check gender requirements
        if (plugin.getConfigManager().isRequireGender()) {
            if (!plugin.getProfileManager().hasGender(sUuid)) {
                plugin.getLanguageManager().sendMessage(sender, "gender-not-set");
                plugin.getLanguageManager().sendRawMessage(sender, "gender-select-prompt");
                return false;
            }
            if (!plugin.getProfileManager().hasGender(rUuid)) {
                plugin.getLanguageManager().sendMessage(sender, "target-gender-not-set", "target", receiver.getName());
                return false;
            }
        }

        // Check proposal cooldown
        int cooldownSeconds = plugin.getConfigManager().getProposalCooldown();
        long lastSent = proposalCooldowns.getOrDefault(sUuid, 0L);
        long elapsed = (System.currentTimeMillis() - lastSent) / 1000L;
        if (elapsed < cooldownSeconds) {
            long remaining = cooldownSeconds - elapsed;
            plugin.getLanguageManager().sendMessage(sender, "proposal-cooldown", "time", String.valueOf(remaining));
            return false;
        }

        // Check if sender already has an outgoing proposal
        for (Proposal existing : proposalsByReceiver.values()) {
            if (existing.getSender().equals(sUuid) && !existing.isExpired(plugin.getConfigManager().getProposalTimeout())) {
                plugin.getLanguageManager().sendMessage(sender, "proposal-already-pending");
                return false;
            }
        }

        // Check if receiver already has a pending incoming proposal
        Proposal existingToReceiver = proposalsByReceiver.get(rUuid);
        if (existingToReceiver != null && !existingToReceiver.isExpired(plugin.getConfigManager().getProposalTimeout())) {
            plugin.getLanguageManager().sendMessage(sender, "proposal-target-pending", "target", receiver.getName());
            return false;
        }

        // Register new proposal
        Proposal proposal = new Proposal(sUuid, rUuid);
        proposalsByReceiver.put(rUuid, proposal);
        proposalCooldowns.put(sUuid, System.currentTimeMillis());

        // Play sounds
        if (plugin.getConfigManager().isPlaySounds()) {
            sender.playSound(sender.getLocation(), plugin.getConfigManager().getSoundProposalSent(), 1.0f, 1.0f);
            receiver.playSound(receiver.getLocation(), plugin.getConfigManager().getSoundProposalReceived(), 1.0f, 1.0f);
        }

        // Send messages
        plugin.getLanguageManager().sendMessage(sender, "proposal-sent", "target", receiver.getName());
        plugin.getLanguageManager().sendRawMessage(receiver, "proposal-received", "sender", sender.getName());
        plugin.getLanguageManager().sendRawMessage(receiver, "proposal-buttons");

        return true;
    }

    public boolean acceptProposal(Player receiver) {
        UUID rUuid = receiver.getUniqueId();
        Proposal proposal = proposalsByReceiver.remove(rUuid);

        if (proposal == null || proposal.isExpired(plugin.getConfigManager().getProposalTimeout())) {
            plugin.getLanguageManager().sendMessage(receiver, "no-pending-proposal");
            return false;
        }

        Player sender = Bukkit.getPlayer(proposal.getSender());
        if (sender == null || !sender.isOnline()) {
            plugin.getLanguageManager().sendMessage(receiver, "player-not-found", "target", "Proposer");
            return false;
        }

        // Check divorce cooldown for receiver
        if (plugin.getProfileManager().isInDivorceCooldown(rUuid)) {
            long remaining = plugin.getProfileManager().getRemainingDivorceCooldownSeconds(rUuid);
            plugin.getLanguageManager().sendMessage(receiver, "divorce-cooldown-active", "time", TimeUtil.formatDuration(remaining));
            return false;
        }

        // Check divorce cooldown for sender
        if (plugin.getProfileManager().isInDivorceCooldown(sender.getUniqueId())) {
            long remaining = plugin.getProfileManager().getRemainingDivorceCooldownSeconds(sender.getUniqueId());
            plugin.getLanguageManager().sendMessage(receiver, "target-divorce-cooldown", "target", sender.getName(), "time", TimeUtil.formatDuration(remaining));
            return false;
        }

        // Check gender requirements
        if (plugin.getConfigManager().isRequireGender()) {
            if (!plugin.getProfileManager().hasGender(rUuid)) {
                plugin.getLanguageManager().sendMessage(receiver, "gender-not-set");
                plugin.getLanguageManager().sendRawMessage(receiver, "gender-select-prompt");
                return false;
            }
            if (!plugin.getProfileManager().hasGender(sender.getUniqueId())) {
                plugin.getLanguageManager().sendMessage(receiver, "target-gender-not-set", "target", sender.getName());
                return false;
            }
        }

        // Double-check if either player married in the meantime
        if (plugin.getMarriageManager().isMarried(sender.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(receiver, "target-already-married", "target", sender.getName());
            return false;
        }
        if (plugin.getMarriageManager().isMarried(receiver.getUniqueId())) {
            plugin.getLanguageManager().sendMessage(receiver, "already-married", "partner", "someone");
            return false;
        }

        plugin.getMarriageManager().createMarriage(sender.getUniqueId(), receiver.getUniqueId());
        return true;
    }

    public boolean denyProposal(Player receiver) {
        UUID rUuid = receiver.getUniqueId();
        Proposal proposal = proposalsByReceiver.remove(rUuid);

        if (proposal == null || proposal.isExpired(plugin.getConfigManager().getProposalTimeout())) {
            plugin.getLanguageManager().sendMessage(receiver, "no-pending-proposal");
            return false;
        }

        Player sender = Bukkit.getPlayer(proposal.getSender());
        String senderName = sender != null ? sender.getName() : Bukkit.getOfflinePlayer(proposal.getSender()).getName();

        plugin.getLanguageManager().sendMessage(receiver, "proposal-denied-receiver", "sender", senderName != null ? senderName : "player");

        if (sender != null && sender.isOnline()) {
            if (plugin.getConfigManager().isPlaySounds()) {
                sender.playSound(sender.getLocation(), plugin.getConfigManager().getSoundProposalDenied(), 1.0f, 1.0f);
            }
            plugin.getLanguageManager().sendMessage(sender, "proposal-denied-sender", "target", receiver.getName());
        }

        return true;
    }

    public void removeProposalsFor(UUID playerUuid) {
        proposalsByReceiver.remove(playerUuid);
        proposalsByReceiver.values().removeIf(p -> p.getSender().equals(playerUuid));
    }

    private void cleanupExpired() {
        int timeout = plugin.getConfigManager().getProposalTimeout();
        Iterator<Map.Entry<UUID, Proposal>> it = proposalsByReceiver.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, Proposal> entry = it.next();
            Proposal p = entry.getValue();
            if (p.isExpired(timeout)) {
                it.remove();

                Player sender = Bukkit.getPlayer(p.getSender());
                Player receiver = Bukkit.getPlayer(p.getReceiver());

                if (sender != null && sender.isOnline()) {
                    String rName = receiver != null ? receiver.getName() : "player";
                    plugin.getLanguageManager().sendMessage(sender, "proposal-expired-sender", "target", rName);
                }
                if (receiver != null && receiver.isOnline()) {
                    String sName = sender != null ? sender.getName() : "player";
                    plugin.getLanguageManager().sendMessage(receiver, "proposal-expired-receiver", "sender", sName);
                }
            }
        }
    }
}
