package com.arcaneminecraft.survival;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

final class PlayerJoin {
	public static final String HR = StringUtils.repeat(" ", 23);
	
	static final boolean sendWelcomeMessage(Player p) {
		p.sendMessage("");
		p.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + HR
				+ ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD + " Arcane Survival "
				+ ChatColor.RESET + ChatColor.GRAY + ChatColor.STRIKETHROUGH + HR);
	    p.sendMessage("");
	    
	    // help
	    TextComponent help = new TextComponent("         ");
	    TextComponent a = new TextComponent("You can type ");
	    a.setColor(ChatColor.WHITE);
	    TextComponent b = new TextComponent("/help");
	    b.setColor(ChatColor.GOLD);
	    b.setBold(true);
	    a.addExtra(b);
	    b = new TextComponent(" for a list of commands.");
	    b.setColor(ChatColor.WHITE);
	    a.addExtra(b);
	    a.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/help"));
	    help.addExtra(a);
	    p.spigot().sendMessage(help);
	    
	    // Website
	    TextComponent website = new TextComponent("       ");
	    a = new TextComponent("Visit our website at ");
	    a.setColor(ChatColor.WHITE);
	    b = new TextComponent("https://arcaneminecraft.com/");
	    b.setColor(ChatColor.GOLD);
	    a.addExtra(b);
	    b = new TextComponent("!");
	    b.setColor(ChatColor.WHITE);
	    a.addExtra(b);
	    a.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://arcaneminecraft.com/"));
	    website.addExtra(a);
	    p.spigot().sendMessage(website);
	    
	    p.sendMessage("");
	    p.sendMessage(ChatColor.GRAY.toString() 
	    		+ ChatColor.STRIKETHROUGH + "    " + HR + HR + HR);
	    p.sendMessage("");
	    return true;
	}
	
	static final boolean sendUnlistedMessage(Player p) {
		p.sendMessage(ChatColor.RED + "[Notice] " + ChatColor.GRAY + ChatColor.DARK_RED + "You do not have build permissions!");
		p.sendMessage(ChatColor.DARK_RED + "> " + ChatColor.GRAY + "You can ask a staff member for approval in the chat.");
		
		TextComponent apply = new TextComponent(TextComponent.fromLegacyText(
				ChatColor.DARK_RED + "> " + ChatColor.GRAY + "Otherwise, type " + ChatColor.GREEN + "/apply" + ChatColor.GRAY + " to apply via our application."
				));
		apply.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/apply"));
		p.spigot().sendMessage(apply);
		
		p.sendMessage("");
		
		return true;
	}
}
