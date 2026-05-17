package com.investplugin;

import com.investplugin.commands.InvestCommand;
import com.investplugin.listeners.ChatListener;
import com.investplugin.listeners.MenuListener;
import com.investplugin.managers.InvestManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class InvestPlugin extends JavaPlugin {

    private static InvestPlugin instance;
    private Economy economy;
    private InvestManager investManager;
    private BukkitTask payoutTask;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("Vault/Economy not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        investManager = new InvestManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Register commands
        InvestCommand investCommand = new InvestCommand(this);
        getCommand("invest").setExecutor(investCommand);
        getCommand("invest").setTabCompleter(investCommand);

        // Start payout task
        startPayoutTask();

        getLogger().info("InvestPlugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (payoutTask != null) {
            payoutTask.cancel();
        }
        if (investManager != null) {
            investManager.saveData();
        }
        getLogger().info("InvestPlugin disabled. Data saved.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    private void startPayoutTask() {
        long interval = getConfig().getLong("payout-interval", 20L);
        payoutTask = getServer().getScheduler().runTaskTimer(this, () -> {
            investManager.processPayout();
        }, interval, interval);
    }

    public void reloadPlugin() {
        reloadConfig();
        if (payoutTask != null) payoutTask.cancel();
        startPayoutTask();
    }

    public static InvestPlugin getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public InvestManager getInvestManager() {
        return investManager;
    }

    public String getMessage(String key) {
        String msg = getConfig().getString("messages." + key, "&cMessage not found: " + key);
        return colorize(getConfig().getString("messages.prefix", "") + msg);
    }

    public String getRawMessage(String key) {
        return colorize(getConfig().getString("messages." + key, "&cMessage not found: " + key));
    }

    public static String colorize(String text) {
        return text.replace("&", "\u00A7");
    }
}
