package com.arcaneminecraft.survival;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

final class PlayerJoin {
	public static final String HR = ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 23);
	
	static final boolean sendWelcomeMessage(Player p) {
		p.sendMessage("");
		p.sendMessage(ChatColor.GRAY + HR + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD + " Arcane Survival " + ChatColor.GRAY + HR);
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
	    TextComponent website = new TextComponent("     ");
	    a = new TextComponent("     Visit our website at ");
	    a.setColor(ChatColor.WHITE);
	    b = new TextComponent("https://arcaneminecraft.com");
	    b.setColor(ChatColor.GOLD);
	    a.addExtra(b);
	    b = new TextComponent("!");
	    b.setColor(ChatColor.WHITE);
	    a.addExtra(b);
	    a.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://arcaneminecraft.com/"));
	    website.addExtra(a);
	    p.spigot().sendMessage(website);
	    
	    p.sendMessage("");
	    p.sendMessage(ChatColor.GRAY + HR + HR + HR);
	    p.sendMessage("");
	    return true;
	}
	
	static final boolean sendUnlistedMessage(Player p) {
		String tag = ChatColor.RED + " [Notice] " + ChatColor.GRAY;
		
		p.sendMessage(tag + ChatColor.DARK_RED + "You do not have build permissions!");
		p.sendMessage("");
		p.sendMessage(tag + "You can ask a staff member for approval in the chat.");
		
		TextComponent apply = new TextComponent(TextComponent.fromLegacyText(tag));
		TextComponent a = new TextComponent(TextComponent.fromLegacyText(ChatColor.GRAY + "Otherwise, you can type " + ChatColor.GREEN + "/apply" + ChatColor.GRAY + " to apply via our application."));
		a.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/apply"));
		apply.addExtra(a);
		p.spigot().sendMessage(apply);
		
		p.sendMessage("");
		
		return true;
	}
}
