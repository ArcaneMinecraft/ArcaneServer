package com.arcaneminecraft.survival;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.arcaneminecraft.ArcaneCommons;
import com.arcaneminecraft.ColorPalette;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

final class PlayerJoin implements Listener {
	static final String HR = StringUtils.repeat(" ", 23);
	final ArcaneSurvival plugin;
	
	PlayerJoin(ArcaneSurvival plugin) {
		this.plugin = plugin;
	}
	
	// Low priority for this; normal for donor, high for mod
	@EventHandler (priority=EventPriority.LOW)
	public void playerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		// Send Splash Message
		sendWelcomeMessage(p);
		
		// Send non-greylisted message
		if (p.hasPermission("arcane.new"))
			sendUnlistedMessage(p);
		
		// First join message
		if (!p.hasPlayedBefore())
			plugin.getServer().broadcastMessage(ColorPalette.META + p.getName()
					+ " has joined Arcane for the first time");
	}
	
	final boolean sendWelcomeMessage(Player p) {
		p.sendMessage("");
		p.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + HR
				+ ChatColor.RESET + ColorPalette.HEADING + ChatColor.BOLD + " Arcane Survival "
				+ ChatColor.RESET + ChatColor.GRAY + ChatColor.STRIKETHROUGH + HR);
	    p.sendMessage("");
	    
	    // help
	    TextComponent help = new TextComponent("         ");
	    TextComponent a = new TextComponent("You can type ");
	    a.setColor(ColorPalette.FOCUS);
	    TextComponent b = new TextComponent("/help");
	    b.setColor(ColorPalette.HEADING);
	    b.setBold(true);
	    a.addExtra(b);
	    a.addExtra(" for a list of commands.");
	    a.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/help"));
	    help.addExtra(a);
	    p.spigot().sendMessage(help);
	    
	    // Website
	    TextComponent website = new TextComponent("       ");
	    a = new TextComponent("Visit our website at ");
	    a.setColor(ColorPalette.FOCUS);
	    b = new TextComponent("https://arcaneminecraft.com/");
	    b.setColor(ColorPalette.HEADING);
	    a.addExtra(b);
	    a.addExtra("!");
	    a.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://arcaneminecraft.com/"));
	    website.addExtra(a);
	    p.spigot().sendMessage(website);
	    
	    p.sendMessage("");
	    p.sendMessage(ChatColor.GRAY.toString() 
	    		+ ChatColor.STRIKETHROUGH + "    " + HR + HR + HR);
	    p.sendMessage("");
	    return true;
	}
	
	final boolean sendUnlistedMessage(Player p) {
		TextComponent msg = ArcaneCommons.tagTC("Notice");
		
		msg.addExtra("You do ");
		
		TextComponent not = new TextComponent("not");
		not.setColor(ColorPalette.NEGATIVE);
		msg.addExtra(not);
		msg.addExtra(" have build permissions!\n Type ");
		
		TextComponent apply = new TextComponent("/apply");
		apply.setColor(ColorPalette.POSITIVE);
		msg.addExtra(apply);
		msg.addExtra(" to apply via our application.");
		
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/apply"));
		p.spigot().sendMessage(msg);
		
		p.sendMessage(ColorPalette.CONTENT + " You can ask a staff member to approve your application.");
		p.sendMessage("");
		
		return true;
	}
}
