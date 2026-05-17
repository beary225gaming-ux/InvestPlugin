package com.investplugin.gui;

import com.investplugin.InvestPlugin;
import com.investplugin.managers.InvestManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class MenuBuilder {

    private static final String MAIN_TITLE = InvestPlugin.colorize("&8&l» &6&lInvestment Menu &8&l«");
    private static final String CONFIRM_TITLE = InvestPlugin.colorize("&8&l» &e&lConfirm Investment &8&l«");

    // ── Slot constants (0-indexed) ────────────────────────────────────────────
    // Rows: 0-8 = top, 9-17 = middle, 18-26 = bottom
    // Middle row slots: 9-17
    // "Slot 2" in middle row = index 10 (9 + 1)
    // "Slot 5" in middle row = index 13 (9 + 4)
    // "Slot 8" in middle row = index 16 (9 + 7)
    // "Below slot 8" = index 25 (18 + 7)

    public static final int MAIN_DELETE_SLOT = 10;   // 2nd slot middle row
    public static final int MAIN_INFO_SLOT = 13;     // 5th slot middle row
    public static final int MAIN_COLLECT_SLOT = 16;  // 8th slot middle row
    public static final int MAIN_AUTO_SLOT = 25;     // below 8th slot (bottom row, 8th)

    public static final int CONFIRM_CANCEL_SLOT = 10;  // 2nd slot middle row
    public static final int CONFIRM_INFO_SLOT = 13;    // 5th slot middle row
    public static final int CONFIRM_OK_SLOT = 16;      // 8th slot middle row

    // ── Main menu ─────────────────────────────────────────────────────────────

    public static Inventory buildMainMenu(Player player, InvestPlugin plugin) {
        InvestManager manager = plugin.getInvestManager();
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_TITLE);

        fillBorder(inv);

        // Slot 2 (index 10): Delete – Redstone Block
        double invested = manager.getInvested(player.getUniqueId());
        ItemStack deleteItem = createItem(
                Material.REDSTONE_BLOCK,
                InvestPlugin.colorize("&c&lDelete"),
                List.of(
                        InvestPlugin.colorize("&7100% of your investments"),
                        InvestPlugin.colorize("&7will be permanently deleted."),
                        InvestPlugin.colorize("&7You will &cnot &7receive any money back."),
                        "",
                        InvestPlugin.colorize("&c&lClick to delete")
                )
        );
        inv.setItem(MAIN_DELETE_SLOT, deleteItem);

        // Slot 5 (index 13): Info – Paper
        double claimable = manager.getClaimable(player.getUniqueId());
        double perSec = manager.getEarningsPerSecond(player.getUniqueId());
        double maxInvest = manager.getMaxInvest();
        String maxStr = maxInvest <= 0 ? "Unlimited" : manager.formatMoney(maxInvest);

        ItemStack infoItem = createItem(
                Material.PAPER,
                InvestPlugin.colorize("&e&lInvestment Info"),
                List.of(
                        InvestPlugin.colorize("&7Invested: &6$" + manager.formatMoney(invested)),
                        InvestPlugin.colorize("&7Earnings/sec: &a$" + manager.formatMoney(perSec)),
                        InvestPlugin.colorize("&7Claimable: &a$" + manager.formatMoney(claimable)),
                        InvestPlugin.colorize("&7Max Investment: &e" + maxStr),
                        "",
                        InvestPlugin.colorize("&eClick to add more investment")
                )
        );
        inv.setItem(MAIN_INFO_SLOT, infoItem);

        // Slot 8 (index 16): Collect – Chest
        ItemStack collectItem = createItem(
                Material.CHEST,
                InvestPlugin.colorize("&a&lCollect"),
                List.of(
                        InvestPlugin.colorize("&7Claimable: &a$" + manager.formatMoney(claimable)),
                        "",
                        InvestPlugin.colorize("&aClick to collect your earnings")
                )
        );
        inv.setItem(MAIN_COLLECT_SLOT, collectItem);

        // Below slot 8 (index 25): Auto-collect toggle
        boolean autoOn = manager.isAutoCollect(player.getUniqueId());
        Material glassMat = autoOn ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String status = autoOn ? InvestPlugin.colorize("&a&lON") : InvestPlugin.colorize("&c&lOFF");
        ItemStack autoItem = createItem(
                glassMat,
                InvestPlugin.colorize("&fAuto-Collect: " + status),
                List.of(
                        InvestPlugin.colorize("&7Automatically collect earnings"),
                        InvestPlugin.colorize("&7as soon as they are generated."),
                        "",
                        InvestPlugin.colorize("&7Status: " + status),
                        InvestPlugin.colorize("&7Click to toggle")
                )
        );
        inv.setItem(MAIN_AUTO_SLOT, autoItem);

        return inv;
    }

    // ── Confirm menu ──────────────────────────────────────────────────────────

    public static Inventory buildConfirmMenu(double amount, InvestPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, CONFIRM_TITLE);

        fillBorder(inv);

        // Slot 2 (index 10): Cancel – Red glass pane
        ItemStack cancelItem = createItem(
                Material.RED_STAINED_GLASS_PANE,
                InvestPlugin.colorize("&c&lCancel"),
                List.of(
                        InvestPlugin.colorize("&7Click to cancel this investment.")
                )
        );
        inv.setItem(CONFIRM_CANCEL_SLOT, cancelItem);

        // Slot 5 (index 13): Amount info – Paper
        InvestManager manager = plugin.getInvestManager();
        ItemStack amountItem = createItem(
                Material.PAPER,
                InvestPlugin.colorize("&e&lInvestment Amount"),
                List.of(
                        InvestPlugin.colorize("&7Amount: &6$" + manager.formatMoney(amount)),
                        InvestPlugin.colorize("&7Earnings/sec: &a$" + manager.formatMoney(amount * manager.getRate())),
                        "",
                        InvestPlugin.colorize("&7Confirm or cancel below.")
                )
        );
        inv.setItem(CONFIRM_INFO_SLOT, amountItem);

        // Slot 8 (index 16): Confirm – Green glass pane
        ItemStack confirmItem = createItem(
                Material.GREEN_STAINED_GLASS_PANE,
                InvestPlugin.colorize("&a&lConfirm"),
                List.of(
                        InvestPlugin.colorize("&7Click to confirm investing"),
                        InvestPlugin.colorize("&6$" + manager.formatMoney(amount) + "&7.")
                )
        );
        inv.setItem(CONFIRM_OK_SLOT, confirmItem);

        return inv;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void fillBorder(Inventory inv) {
        ItemStack filler = createItem(
                Material.GRAY_STAINED_GLASS_PANE,
                InvestPlugin.colorize("&8"),
                List.of()
        );
        int size = inv.getSize();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            boolean isBorder = (row == 0 || row == rows - 1 || col == 0 || col == 8);
            if (isBorder) {
                inv.setItem(i, filler);
            }
        }
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getMainTitle() {
        return MAIN_TITLE;
    }

    public static String getConfirmTitle() {
        return CONFIRM_TITLE;
    }
}
