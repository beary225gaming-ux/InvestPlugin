package com.investplugin.managers;

import com.investplugin.InvestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvestManager {

    private final InvestPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    // Player UUID -> invested amount
    private final Map<UUID, Double> investedAmounts = new HashMap<>();
    // Player UUID -> claimable earnings
    private final Map<UUID, Double> claimableEarnings = new HashMap<>();
    // Player UUID -> auto-collect enabled
    private final Map<UUID, Boolean> autoCollect = new HashMap<>();

    public InvestManager(InvestPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    // ── Data persistence ──────────────────────────────────────────────────────

    public void loadData() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.contains("players")) {
            for (String uuidStr : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                double invested = dataConfig.getDouble("players." + uuidStr + ".invested", 0);
                double claimable = dataConfig.getDouble("players." + uuidStr + ".claimable", 0);
                boolean auto = dataConfig.getBoolean("players." + uuidStr + ".auto-collect", false);
                if (invested > 0) investedAmounts.put(uuid, invested);
                if (claimable > 0) claimableEarnings.put(uuid, claimable);
                autoCollect.put(uuid, auto);
            }
        }
        plugin.getLogger().info("Loaded investment data for " + investedAmounts.size() + " players.");
    }

    public void saveData() {
        for (Map.Entry<UUID, Double> entry : investedAmounts.entrySet()) {
            String path = "players." + entry.getKey();
            dataConfig.set(path + ".invested", entry.getValue());
            dataConfig.set(path + ".claimable", claimableEarnings.getOrDefault(entry.getKey(), 0.0));
            dataConfig.set(path + ".auto-collect", autoCollect.getOrDefault(entry.getKey(), false));
        }
        // Also save players with no investment but existing data (claimable/autocollect)
        for (UUID uuid : claimableEarnings.keySet()) {
            if (!investedAmounts.containsKey(uuid)) {
                String path = "players." + uuid;
                dataConfig.set(path + ".invested", 0.0);
                dataConfig.set(path + ".claimable", claimableEarnings.get(uuid));
                dataConfig.set(path + ".auto-collect", autoCollect.getOrDefault(uuid, false));
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    // ── Payout ────────────────────────────────────────────────────────────────

    public void processPayout() {
        double rate = plugin.getConfig().getDouble("invest-rate", 0.001);

    for (Map.Entry<UUID, Double> entry : investedAmounts.entrySet()) {
        UUID uuid = entry.getKey();

        // Only earn while online
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) continue;

        double invested = entry.getValue();
        double earned = invested * rate;

        double current = claimableEarnings.getOrDefault(uuid, 0.0);
        double newTotal = current + earned;
        claimableEarnings.put(uuid, newTotal);

        // Auto-collect
        if (autoCollect.getOrDefault(uuid, false)) {
            collectEarnings(player);
        }
    }

        // Auto-save periodically (every ~5 minutes = 6000 ticks / 20 ticks per call ≈ 300 calls)
        // Simple approach: save every call since it's lightweight
        // Actually save every 30 payouts
    }

    // ── Investment operations ─────────────────────────────────────────────────

    public boolean invest(Player player, double amount) {
        double maxInvest = plugin.getConfig().getDouble("max-invest", 0);
        double current = investedAmounts.getOrDefault(player.getUniqueId(), 0.0);

        if (maxInvest > 0 && current + amount > maxInvest) {
            return false; // exceeds max
        }

        if (!plugin.getEconomy().has(player, amount)) {
            return false;
        }

        plugin.getEconomy().withdrawPlayer(player, amount);
        investedAmounts.put(player.getUniqueId(), current + amount);
        saveData();
        return true;
    }

    public double collectEarnings(Player player) {
        UUID uuid = player.getUniqueId();
        double claimable = claimableEarnings.getOrDefault(uuid, 0.0);
        if (claimable <= 0) return 0;

        plugin.getEconomy().depositPlayer(player, claimable);
        claimableEarnings.put(uuid, 0.0);
        saveData();
        return claimable;
    }

    public void deleteInvestment(Player player) {
        UUID uuid = player.getUniqueId();
        investedAmounts.remove(uuid);
        saveData();
    }

    public void toggleAutoCollect(Player player) {
        UUID uuid = player.getUniqueId();
        boolean current = autoCollect.getOrDefault(uuid, false);
        autoCollect.put(uuid, !current);
        saveData();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public double getInvested(UUID uuid) {
        return investedAmounts.getOrDefault(uuid, 0.0);
    }

    public double getClaimable(UUID uuid) {
        return claimableEarnings.getOrDefault(uuid, 0.0);
    }

    public boolean isAutoCollect(UUID uuid) {
        return autoCollect.getOrDefault(uuid, false);
    }

    public double getRate() {
        return plugin.getConfig().getDouble("invest-rate", 0.001);
    }

    public double getMaxInvest() {
        return plugin.getConfig().getDouble("max-invest", 0);
    }

    public double getEarningsPerSecond(UUID uuid) {
        return getInvested(uuid) * getRate();
    }

    public String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }
}
