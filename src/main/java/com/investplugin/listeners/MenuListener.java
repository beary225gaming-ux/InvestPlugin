package com.investplugin.listeners;

import com.investplugin.InvestPlugin;
import com.investplugin.gui.MenuBuilder;
import com.investplugin.managers.InvestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    private final InvestPlugin plugin;

    public MenuListener(InvestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();

        if (title.equals(MenuBuilder.getMainTitle())) {
            handleMainMenu(event, player);
        } else if (title.equals(MenuBuilder.getConfirmTitle())) {
            handleConfirmMenu(event, player);
        } else if (title.equals(MenuBuilder.getDeleteConfirmTitle())) {
            handleDeleteConfirmMenu(event, player);
        }
    }

    private void handleMainMenu(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        InvestManager manager = plugin.getInvestManager();

        if (slot == MenuBuilder.MAIN_DELETE_SLOT) {
            double invested = manager.getInvested(player.getUniqueId());
            if (invested <= 0) {
                player.sendMessage(plugin.getMessage("nothing-to-collect"));
                player.closeInventory();
                return;
            }
            // Open delete confirmation menu instead of deleting immediately
            player.openInventory(MenuBuilder.buildDeleteConfirmMenu(invested));

        } else if (slot == MenuBuilder.MAIN_INFO_SLOT) {
            player.closeInventory();
            player.sendMessage(plugin.getMessage("enter-amount"));
            ChatListener.awaitingInput.add(player.getUniqueId());

        } else if (slot == MenuBuilder.MAIN_COLLECT_SLOT) {
            double collected = manager.collectEarnings(player);
            player.closeInventory();
            if (collected <= 0) {
                player.sendMessage(plugin.getMessage("nothing-to-collect"));
            } else {
                player.sendMessage(plugin.getMessage("collect-success")
                        .replace("{amount}", MenuBuilder.formatShort(collected)));
            }

        } else if (slot == MenuBuilder.MAIN_AUTO_SLOT) {
            manager.toggleAutoCollect(player);
            boolean isOn = manager.isAutoCollect(player.getUniqueId());
            if (isOn) {
                player.sendMessage(plugin.getMessage("auto-collect-on"));
            } else {
                player.sendMessage(plugin.getMessage("auto-collect-off"));
            }
            player.openInventory(MenuBuilder.buildMainMenu(player, plugin));
        }
    }

    private void handleConfirmMenu(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        InvestManager manager = plugin.getInvestManager();

        if (slot == MenuBuilder.CONFIRM_CANCEL_SLOT) {
            ChatListener.pendingAmount.remove(player.getUniqueId());
            player.closeInventory();
            player.sendMessage(plugin.getMessage("invest-cancelled"));

        } else if (slot == MenuBuilder.CONFIRM_OK_SLOT) {
            Double amount = ChatListener.pendingAmount.remove(player.getUniqueId());
            player.closeInventory();

            if (amount == null) {
                player.sendMessage(plugin.getMessage("invest-cancelled"));
                return;
            }

            double maxInvest = manager.getMaxInvest();
            double currentInvested = manager.getInvested(player.getUniqueId());
            if (maxInvest > 0 && currentInvested + amount > maxInvest) {
                player.sendMessage(plugin.getMessage("exceeds-max")
                        .replace("{max}", MenuBuilder.formatShort(maxInvest)));
                return;
            }

            boolean success = manager.invest(player, amount);
            if (success) {
                double newTotal = manager.getInvested(player.getUniqueId());
                player.sendMessage(plugin.getMessage("invest-success")
                        .replace("{amount}", MenuBuilder.formatShort(amount))
                        .replace("{total}", MenuBuilder.formatShort(newTotal)));
            } else {
                player.sendMessage(plugin.getMessage("not-enough-money"));
            }
        }
    }

    private void handleDeleteConfirmMenu(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        InvestManager manager = plugin.getInvestManager();

        if (slot == MenuBuilder.DELETE_CANCEL_SLOT) {
            // Go back to main menu
            player.openInventory(MenuBuilder.buildMainMenu(player, plugin));

        } else if (slot == MenuBuilder.DELETE_CONFIRM_SLOT) {
            double invested = manager.getInvested(player.getUniqueId());
            manager.deleteInvestment(player);
            player.closeInventory();
            player.sendMessage(plugin.getMessage("investment-deleted")
                    .replace("{amount}", MenuBuilder.formatShort(invested)));
        }
    }
}
