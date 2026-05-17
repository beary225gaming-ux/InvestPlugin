package com.investplugin.listeners;

import com.investplugin.InvestPlugin;
import com.investplugin.gui.MenuBuilder;
import com.investplugin.managers.InvestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChatListener implements Listener {

    private final InvestPlugin plugin;

    // Players who are currently being asked to type an amount
    public static final Set<UUID> awaitingInput = new HashSet<>();
    // Players who have typed an amount and are on the confirm screen
    public static final Map<UUID, Double> pendingAmount = new HashMap<>();

    public ChatListener(InvestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingInput.contains(uuid)) return;

        event.setCancelled(true);
        awaitingInput.remove(uuid);

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.getMessage("invest-cancelled"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(message);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getMessage("invalid-amount"));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(plugin.getMessage("invalid-amount"));
            return;
        }

        // Validate on main thread before opening GUI
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            InvestManager manager = plugin.getInvestManager();

            // Check they have the money
            if (!plugin.getEconomy().has(player, amount)) {
                player.sendMessage(plugin.getMessage("not-enough-money"));
                return;
            }

            // Check max invest
            double maxInvest = manager.getMaxInvest();
            double currentInvested = manager.getInvested(uuid);
            if (maxInvest > 0 && currentInvested + amount > maxInvest) {
                player.sendMessage(plugin.getMessage("exceeds-max")
                        .replace("{max}", manager.formatMoney(maxInvest)));
                return;
            }

            // Store pending amount and open confirm menu
            pendingAmount.put(uuid, amount);
            player.openInventory(MenuBuilder.buildConfirmMenu(amount, plugin));
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        awaitingInput.remove(uuid);
        pendingAmount.remove(uuid);
    }
}
