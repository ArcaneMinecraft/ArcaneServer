package com.arcaneminecraft.survival;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

final class PlayerJoin implements CommandExecutor, Listener {
	private static final String NEWS_TAG = "News";
	private static final String NEWS_CONFIG = "news";
	private final TextComponent[] joinText = new TextComponent[2];
	static final String HR = StringUtils.repeat(" ", 23);
	final ArcaneSurvival plugin;

	private String newsMsg;
	
	PlayerJoin(ArcaneSurvival plugin) {
		this.plugin = plugin;
		
		// News
		this.newsMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(NEWS_CONFIG));
		
	    // help
	    joinText[0] = new TextComponent("         ");
	    TextComponent a = new TextComponent("You can type ");
	    a.setColor(ColorPalette.FOCUS);
	    TextComponent b = new TextComponent("/help");
	    b.setColor(ColorPalette.HEADING);
	    b.setBold(true);
	    a.addExtra(b);
	    a.addExtra(" for a list of commands.");
	    a.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/help"));
	    joinText[0].addExtra(a);
	    
	    // Website
	    joinText[1] = new TextComponent("       ");
	    a = new TextComponent("Visit our website at ");
	    a.setColor(ColorPalette.FOCUS);
	    b = new TextComponent("https://arcaneminecraft.com/");
	    b.setColor(ColorPalette.HEADING);
	    a.addExtra(b);
	    a.addExtra("!");
	    a.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://arcaneminecraft.com/"));
	    joinText[1].addExtra(a);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("news")) {
			if (args.length == 0) {
				if (newsMsg == null)
					sender.sendMessage(ArcaneCommons.tag(NEWS_TAG, ChatColor.ITALIC + "(No News)"));
				else
					sender.sendMessage(ArcaneCommons.tag(NEWS_TAG, newsMsg));
			}
			
			if (sender.hasPermission("arcane.command.newsmod")) {
				if (args.length == 0) {
					sender.sendMessage(ArcaneCommons.tag(NEWS_TAG, "Usage: /news (set|clear) [<new news...>]"));
					return true;
				}
				if (args[0].equalsIgnoreCase("set")) {
					if (args.length == 1) {
						sender.sendMessage(ArcaneCommons.tag(NEWS_TAG, "Usage: /news set <new news...>"));
						return true;
					}
					StringBuilder n = new StringBuilder(args[1]);
					for (int i = 2; i < args.length; i++)
						n.append(' ').append(args[i]);
					String s = n.toString(); 
					plugin.getConfig().set(NEWS_CONFIG, s);
					s = ChatColor.translateAlternateColorCodes('&', s);
					newsMsg = s;
					sender.sendMessage(ArcaneCommons.tag(NEWS_TAG, "News set: " + s));
					return true;
				}
				if (args[0].equalsIgnoreCase("clear")) {
					plugin.getConfig().set(NEWS_CONFIG, null);
					newsMsg = null;
					sender.sendMessage(ArcaneCommons.tag(NEWS_TAG, "News cleared."));
					return true;
				}
				
				sender.sendMessage(ArcaneCommons.tag(NEWS_TAG, "Admin Usage: /news (set|clear) [<new news...>]"));
				return true;
			}
			
			if (args.length != 0)
				sender.sendMessage(ArcaneCommons.noPermissionMsg(label,args[0]));
			
			return true;
		}
		return false;
	}
	
	// Low priority for this; normal for donor message
	@EventHandler (priority=EventPriority.LOW)
	public void joinMessage(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		// Send Splash Message
		sendWelcomeMessage(p);
	}
	
	@EventHandler (priority=EventPriority.HIGH)
	public void news(PlayerJoinEvent e) {
		if (newsMsg != null) {
			Player p = e.getPlayer();
			p.sendMessage(ArcaneCommons.tag(NEWS_TAG, newsMsg));
			p.sendMessage("");
		}
	}
	
	final void sendWelcomeMessage(Player p) {
		p.sendMessage("");
		p.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + HR
				+ ChatColor.RESET + ColorPalette.HEADING + ChatColor.BOLD + " Arcane Survival "
				+ ChatColor.RESET + ChatColor.GRAY + ChatColor.STRIKETHROUGH + HR);
		p.sendMessage("");

		p.spigot().sendMessage(joinText[0]);
		p.spigot().sendMessage(joinText[1]);

		p.sendMessage("");
		p.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "    " + HR + HR + HR);
		p.sendMessage("");
	}
}
