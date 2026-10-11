package com.zornoely.nofireworkoffhand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class NoFireworkOffhand extends JavaPlugin implements Listener {

    private final String ERROR_MESSAGE = ChatColor.RED + "Havai fişeği sol eline alamazsın!";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Arka planda periyodik kontrol: Bir şekilde sol ele girerse anında kaldırır
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkAndRemoveOffhandFirework(player);
                }
            }
        }.runTaskTimer(this, 0L, 5L);

        getLogger().info("NoFireworkOffhand aktif edildi!");
    }

    @Override
    public void onDisable() {
        getLogger().info("NoFireworkOffhand deaktif edildi.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        ItemStack mainHand = event.getOffHandItem();
        if (mainHand != null && mainHand.getType() == Material.FIREWORK_ROCKET) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ERROR_MESSAGE);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() == Material.FIREWORK_ROCKET) {
                event.setCancelled(true);
                player.sendMessage(ERROR_MESSAGE);
                return;
            }
        }

        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR && event.getSlot() == 40) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() == Material.FIREWORK_ROCKET) {
                event.setCancelled(true);
                player.sendMessage(ERROR_MESSAGE);
            }
        }
    }

    private void checkAndRemoveOffhandFirework(Player player) {
        if (player == null || !player.isOnline()) return;

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() == Material.FIREWORK_ROCKET) {
            player.getInventory().setItemInOffHand(null);
            var leftover = player.getInventory().addItem(offhand);
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
            player.sendMessage(ERROR_MESSAGE);
        }
    }
}
