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
			p.setPlayerListName(ColorPalette.ADMIN + "Admin" + ChatColor.DARK_GRAY + " | " + ColorPalette.RESET + p.getPlayerListName());
		else if (p.hasPermission("arcane.mod"))
			p.setPlayerListName(ColorPalette.MOD + "Mod" + ChatColor.DARK_GRAY + " | " + ColorPalette.RESET + p.getPlayerListName());
		else if (p.hasPermission("arcane.chatmod"))
			p.setPlayerListName(ColorPalette.CHAT_MOD + "ChatMod" + ChatColor.DARK_GRAY + " | " + ColorPalette.RESET + p.getPlayerListName());
		else if (p.hasPermission("arcane.donor"))
			p.setPlayerListName(ColorPalette.DONOR + "ChatMod" + ChatColor.DARK_GRAY + " | " + ColorPalette.RESET + p.getPlayerListName());

	}
	
	@EventHandler (priority=EventPriority.HIGHEST)
	public void joinAddRole(PlayerJoinEvent e) {
		addRole(e.getPlayer());
	}
}
