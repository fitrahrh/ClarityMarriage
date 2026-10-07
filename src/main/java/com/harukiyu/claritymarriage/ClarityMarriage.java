package com.harukiyu.claritymarriage;

import com.harukiyu.claritymarriage.commands.AdminCommand;
import com.harukiyu.claritymarriage.commands.MarryCommand;
import com.harukiyu.claritymarriage.commands.MarryTabCompleter;
import com.harukiyu.claritymarriage.gui.BankGui;
import com.harukiyu.claritymarriage.hooks.MarriagePlaceholderExpansion;
import com.harukiyu.claritymarriage.hooks.VaultHook;
import com.harukiyu.claritymarriage.listeners.BankGuiListener;
import com.harukiyu.claritymarriage.listeners.LoveExpListener;
import com.harukiyu.claritymarriage.listeners.PlayerChatListener;
import com.harukiyu.claritymarriage.listeners.PlayerConnectionListener;
import com.harukiyu.claritymarriage.listeners.PlayerDamageListener;
import com.harukiyu.claritymarriage.listeners.PlayerMoveListener;
import com.harukiyu.claritymarriage.managers.BankManager;
import com.harukiyu.claritymarriage.managers.ChatManager;
import com.harukiyu.claritymarriage.managers.ConfigManager;
import com.harukiyu.claritymarriage.managers.LanguageManager;
import com.harukiyu.claritymarriage.managers.LoveExpManager;
import com.harukiyu.claritymarriage.managers.MarriageManager;
import com.harukiyu.claritymarriage.managers.PlaytimeManager;
import com.harukiyu.claritymarriage.managers.ProfileManager;
import com.harukiyu.claritymarriage.managers.ProposalManager;
import com.harukiyu.claritymarriage.managers.TeleportManager;
import com.harukiyu.claritymarriage.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for ClarityMarriage.
 */
public class ClarityMarriage extends JavaPlugin {

    private static ClarityMarriage instance;

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private StorageManager storageManager;
    private MarriageManager marriageManager;
    private ProfileManager profileManager;
    private ProposalManager proposalManager;
    private TeleportManager teleportManager;
    private ChatManager chatManager;
    private PlaytimeManager playtimeManager;
    private VaultHook vaultHook;
    private BankManager bankManager;
    private BankGui bankGui;
    private LoveExpManager loveExpManager;
    private MarriagePlaceholderExpansion placeholderExpansion;

    public static ClarityMarriage getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        long startTime = System.currentTimeMillis();
        getLogger().info("Starting ClarityMarriage initialization...");

        // 1. Initialize configuration and messages
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.languageManager = new LanguageManager(this);
        this.languageManager.load();

        // 2. Initialize storage, profiles, and load marriages
        this.storageManager = new StorageManager(this);
        this.storageManager.init();

        this.profileManager = new ProfileManager(this);
        this.profileManager.load();

        this.marriageManager = new MarriageManager(this);
        this.marriageManager.load();

        // 3. Initialize Vault economy
        this.vaultHook = new VaultHook();
        if (this.vaultHook.init()) {
            getLogger().info("Hooked into Vault Economy successfully!");
        } else {
            getLogger().info("Vault Economy not found. Bank commands will be restricted until an economy provider is installed.");
        }

        // 4. Initialize feature managers
        this.proposalManager = new ProposalManager(this);
        this.proposalManager.start();

        this.teleportManager = new TeleportManager(this);
        this.chatManager = new ChatManager(this);

        this.playtimeManager = new PlaytimeManager(this);
        this.playtimeManager.start();

        this.bankManager = new BankManager(this);
        this.bankGui = new BankGui(this);
        this.bankGui.load();

        this.loveExpManager = new LoveExpManager(this);
        this.loveExpManager.start();

        // 5. Register event listeners
        registerListeners();

        // 6. Register commands
        registerCommands();

        // 7. Optional PlaceholderAPI Hook
        registerPlaceholderApi();

        long elapsed = System.currentTimeMillis() - startTime;
        getLogger().info(String.format("ClarityMarriage v%s successfully enabled in %dms!", getPluginMeta().getVersion(), elapsed));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ClarityMarriage...");

        // Unregister PlaceholderAPI
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
            placeholderExpansion = null;
        }

        // Stop tasks
        if (proposalManager != null) {
            proposalManager.stop();
        }
        if (playtimeManager != null) {
            playtimeManager.stop();
        }
        if (loveExpManager != null) {
            loveExpManager.stop();
        }

        // Save active data
        if (marriageManager != null) {
            marriageManager.saveAll();
        }
        if (profileManager != null) {
            profileManager.saveAll();
        }

        // Close database/storage connection
        if (storageManager != null) {
            storageManager.close();
        }

        instance = null;
        getLogger().info("ClarityMarriage disabled successfully.");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(this), this);
        pm.registerEvents(new PlayerChatListener(this), this);
        pm.registerEvents(new PlayerDamageListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new LoveExpListener(this), this);
        pm.registerEvents(new BankGuiListener(this), this);
    }

    private void registerCommands() {
        PluginCommand marryCmd = getCommand("marry");
        if (marryCmd != null) {
            MarryCommand cmdExecutor = new MarryCommand(this);
            marryCmd.setExecutor(cmdExecutor);
            marryCmd.setTabCompleter(new MarryTabCompleter(this));
        } else {
            getLogger().log(Level.SEVERE, "Failed to register /marry command! Check plugin.yml");
        }

        PluginCommand adminCmd = getCommand("claritymarriage");
        if (adminCmd != null) {
            AdminCommand adminExecutor = new AdminCommand(this);
            adminCmd.setExecutor(adminExecutor);
            adminCmd.setTabCompleter(adminExecutor);
        } else {
            getLogger().log(Level.SEVERE, "Failed to register /claritymarriage command! Check plugin.yml");
        }
    }

    private void registerPlaceholderApi() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderExpansion = new MarriagePlaceholderExpansion(this);
            if (this.placeholderExpansion.register()) {
                getLogger().info("Hooked into PlaceholderAPI successfully!");
            }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public MarriageManager getMarriageManager() {
        return marriageManager;
    }

    public ProposalManager getProposalManager() {
        return proposalManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public BankManager getBankManager() {
        return bankManager;
    }

    public BankGui getBankGui() {
        return bankGui;
    }

    public LoveExpManager getLoveExpManager() {
        return loveExpManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }
}
