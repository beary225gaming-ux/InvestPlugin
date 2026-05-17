package com.investplugin.commands;

import com.investplugin.InvestPlugin;
import com.investplugin.gui.MenuBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class InvestCommand implements CommandExecutor, TabCompleter {

    private final InvestPlugin plugin;

    public InvestCommand(InvestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(InvestPlugin.colorize("&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission("investplugin.invest")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        // Handle sub-commands for admins
        if (args.length > 0 && player.hasPermission("investplugin.admin")) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadPlugin();
                player.sendMessage(InvestPlugin.colorize("&aInvestPlugin config reloaded!"));
                return true;
            }
        }

        player.openInventory(MenuBuilder.buildMainMenu(player, plugin));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("investplugin.admin")) {
            return List.of("reload");
        }
        return List.of();
    }
}
