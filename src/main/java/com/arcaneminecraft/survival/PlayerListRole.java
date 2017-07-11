package com.arcaneminecraft.survival;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.arcaneminecraft.ColorPalette;

import net.md_5.bungee.api.ChatColor;

public class PlayerListRole implements Listener {
	
	PlayerListRole(ArcaneSurvival plugin) {
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			addRole(p);
		}
	}
	
	private void addRole(Player p) {
		if (p.hasPermission("arcane.admin"))
			p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + ColorPalette.ADMIN + "Admin");
		else if (p.hasPermission("arcane.mod"))
			p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + ColorPalette.MOD + "Mod" + ChatColor.DARK_GRAY);
		else if (p.hasPermission("arcane.chatmod"))
			p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + ColorPalette.CHAT_MOD + "ChatMod" + ChatColor.DARK_GRAY);
		else if (p.hasPermission("arcane.donor"))
			p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + ColorPalette.DONOR + "Donor" + ChatColor.DARK_GRAY);
	}
	
	@EventHandler (priority=EventPriority.HIGHEST)
	public void joinAddRole(PlayerJoinEvent e) {
		addRole(e.getPlayer());
	}
}
