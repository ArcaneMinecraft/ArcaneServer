package com.arcaneminecraft.server;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.arcaneminecraft.api.ColorPalette;

import net.md_5.bungee.api.ChatColor;

public class PlayerListRole implements Listener {

    PlayerListRole(ArcaneServer plugin) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            addRole(p);
        }
    }

    private void addRole(Player p) {
        if (p.hasPermission("arcane.tag.admin"))
            p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + ColorPalette.ADMIN + "Admin");
		else if (p.hasPermission("arcane.tag.mod"))
            p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + ColorPalette.MOD + "Mod" + ChatColor.DARK_GRAY);
        else if (p.hasPermission("arcane.tag.chatmod"))
            p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + ColorPalette.CHAT_MOD + "ChatMod" + ChatColor.DARK_GRAY);
        else if (p.hasPermission("arcane.tag.donor"))
            p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + ColorPalette.DONOR + "Donor" + ChatColor.DARK_GRAY);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void joinAddRole(PlayerJoinEvent e) {
        addRole(e.getPlayer());
    }
}
