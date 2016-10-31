package com.arcaneminecraft.survival;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class HelpLink {
	/**
	 * Function for sending messages
	 * Sends help data to the player in a specific format
	 * @param sender Command Sender
	 * @param header Heading title to use
	 * @param LIST   Help List
	 * @param label  Command label used to call the function
	 * @param subcmd Subcommand; null for no subcommand
	 * @param page   Help page to display
	 * @return
	 */
	public static boolean sendHelp(CommandSender sender, String header, String LIST[][][], String label, String subcmd, int page) {
		Player p = (Player)sender;
		// array position
		int a = page - 1;
		
		// Heading
		p.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + " ----- "
					+ ChatColor.GOLD + ChatColor.BOLD + header
					+ ChatColor.GRAY + ChatColor.BOLD + " (page " + page + "/" + LIST.length + ")"
					+ " -----");
		
		// Body
		// Min/max page check
		if (a < LIST.length && page > 0) {
			// Iterate through each line
			for (int i = 0; i < LIST[a].length; i++) {
				// skip line if 
				if (LIST[a][i].length > 3 && sender.hasPermission(LIST[a][i][3]))
					continue;
				
				TextComponent ret = new TextComponent("> ");
				ret.setColor(ChatColor.GRAY);
				if (LIST[a][i].length == 1) {
					
				} else {
					TextComponent c = new TextComponent(ChatColor.GOLD + "/" + LIST[a][i][0]
							+ ChatColor.DARK_GRAY + " - "
							+ ChatColor.GRAY + LIST[a][i][1]);
					c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND
							, "/" + LIST[a][i][0] + " "));
					if (LIST[a][i].length > 2 && LIST[a][i][2] != null)
						c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
								, new ComponentBuilder(LIST[a][i][2]).create()));
					ret.addExtra(c);
				}
				p.spigot().sendMessage(ret);
			}
		}
		
		// Footer
		TextComponent ft = new TextComponent("" + ChatColor.GRAY + ChatColor.BOLD + " -- "
				+ ChatColor.RESET +ChatColor.GOLD + "Pages: ");
		for (int i = 0; i < LIST.length; i++) {
			int npg = i + 1;
			// Compose a list of commands
			String pgLs = ChatColor.GRAY + "Page " + npg + ":" + ChatColor.GOLD;
			
			for (String c[] : LIST[i])
				pgLs += "\n /" + c[0];
			
			TextComponent pg = new TextComponent("[" + npg + "]");
			if (i == a)
				pg.setColor(ChatColor.DARK_GRAY);
			else
				pg.setColor(ChatColor.GRAY);
			pg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
					, new ComponentBuilder(pgLs).create()));
			pg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND
					, "/" + label + " " + (subcmd == null ? "" : subcmd + " ") + npg));
			ft.addExtra(pg);
			ft.addExtra(" ");
		}
		
		p.spigot().sendMessage(ft);
		return true;
	}
	
	// Main function; filter it
	static boolean commandHelp(CommandSender sender, String args[], String label) {
		// Default
		if (args.length == 0)
			return sendHelp(sender, "General Help", HELP, label, null, 1);
		
		// if numeric
		if (StringUtils.isNumeric(args[0])) {
			return sendHelp(sender, "General Help", HELP, label, null, Integer.parseInt(args[0]));
		}
		
		// if has second argument (must be numeric)
		int page = 1;
		if (args.length > 1 && StringUtils.isNumeric(args[1]))
			page = Integer.parseInt(args[1]);
		
		String subcmd = args[0].toLowerCase();
		switch (subcmd) {
		case "lwc":
			return sendHelp(sender, "LWC Help", LWC, label, subcmd, page);
		case "message":
		case "msg":
			return sendHelp(sender, "Messaging Help", MESSAGE, label, subcmd, page);
		case "donors":
		case "donor":
			if (sender.hasPermission("arcane.mod")) {
				return sendHelp(sender, "Donor Help", DONOR, label, subcmd, page);
			}
			sender.sendMessage(ArcaneSurvival.noPermissionCmd(label,subcmd));
			return true;
		case "chatmods":
		case "chatmod":
			if (sender.hasPermission("arcane.mod") || sender.hasPermission("arcane.chatmod")) {
				return sendHelp(sender, "Chatmod Help", CHATMOD, label, subcmd, page);
			}
			sender.sendMessage(ArcaneSurvival.noPermissionCmd(label,subcmd));
			return true;
		case "mods":
		case "mod":
			if (sender.hasPermission("arcane.mod")) {
				return sendHelp(sender, "Moderator Help", MOD, label, subcmd, page);
			}
			sender.sendMessage(ArcaneSurvival.noPermissionCmd(label,subcmd));
			return true;
		}
		return false;
	}
	
	// Function for getting links
	static boolean commandLink(CommandSender sender) {
		Player p = (Player)sender;
		
		// Heading
		p.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + " ----- "
					+ ChatColor.GOLD + ChatColor.BOLD + "Arcane Links"
					+ ChatColor.GRAY + ChatColor.BOLD + " -----");
		
		// Body
		// Iterate through each line
		for (int i = 0; i < LINK.length; i++) {
			TextComponent ret = new TextComponent("> ");
			ret.setColor(ChatColor.GRAY);
			TextComponent c = new TextComponent(ChatColor.GOLD + "/" + LINK[i][0]
					+ ChatColor.DARK_GRAY + " - "
					+ ChatColor.GRAY + LINK[i][1]);
			c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
					, LINK[i][1]));
			ret.addExtra(c);
			p.spigot().sendMessage(ret);
		}
		
		// Footer
		TextComponent ft = new TextComponent("" + ChatColor.GRAY + ChatColor.BOLD + " -- ");
		TextComponent mw = new TextComponent(ChatColor.GOLD + "Main website: " + ChatColor.GRAY + "https://arcaneminecraft.com/");
		mw.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
				, "https://arcaneminecraft.com/"));
		ft.addExtra(mw);
		p.spigot().sendMessage(ft);
		return true;
	}
	
	static boolean commandSingleLink(CommandSender sender, String label) {
		if (!(sender instanceof Player))
			return false;
		for (String[] ls : LINK) {
			if (ls[0].equals(label.toLowerCase())) {
				TextComponent ret = new TextComponent(ArcaneSurvival.TAG + " ");
				TextComponent ln = new TextComponent(ls[2]);
				ln.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
						, ls[1]));
				ret.addExtra(ln);
				((Player)sender).spigot().sendMessage(ret);
				return true;
			}
		}
			
		return false;
	}
	
	private static final String LINK[][] = {
			{"map","https://arcaneminecraft.com/dynmap/",ChatColor.GRAY + "Click here to view our " + ChatColor.WHITE + "Dynmap" + ChatColor.GRAY + "."},
			{"discord","https://arcaneminecraft.com/discord/",ChatColor.GRAY + "Click here for our " + ChatColor.WHITE + "Discord" + ChatColor.GRAY + " invite link."},
			{"forum","https://arcaneminecraft.com/forum/",ChatColor.GRAY + "Click here to visit our " + ChatColor.WHITE + "forum" + ChatColor.GRAY + "."},
			{"donate","https://arcaneminecraft.com/donate/",ChatColor.GRAY + "To " + ChatColor.WHITE + "donate" + ChatColor.GRAY + " to the Arcane, click here."}
	};
	
	// Seven commands per page.
	// Indexes:
	// 0 - command
	// 1 - description
	// 2 - tooltip
	// 3 - permission required to see entry
	private static final String HELP[][][] = {
			{
				{"help","show this page","/help [1|2|lwc 1|lwc 2|msg|donor]"},
				{"spawn","return to the spawn","/spawn [old | new]"},
				{"home","return to your home","/home [name]"},
				{"sethome","set your home","/sethome [name]"},
				{"kill","temporary ends your suffering just to revive you"},
				{"afk","mark yourself as afk"},
				{"pvp","toggle PvP combat"},
			},
			{
				{"seen","gets last logoff date","/seen [player]"},
				{"seenf","gets first logged on date","/seenf [player]\nAlias:\n /fseen"},
				{"list","lists online players"},
				{"tps","check server ticks per second"},
				{"username", "an advanced username command"},
				{"links","links to arcane sites"},
				{"donors", "list of donor commands", "Alias:\n /donor"}
			},
			{
				{"help message","message commands","Alias:\n /help msg"},
				{"help lwc", "chest/door protection help","More help:\n /lwc"},
				{"help donor", "donor help", "Alias:\n /help donors", "arcane.donor"},
				{"help chatmod", "chat moderator help", "Alias:\n /help chatmods", "arcane.chatmod"},
				{"help mod", "moderator help", "Alias:\n /help mods", "arcane.mod"}
			}
	};
	private static final String MESSAGE[][][] = {
			{
				{"msg","message an online player","/msg <player> <message>\nAlias:\n /m"},
				{"reply","reply to a recently messaged person","/reply <message>\nAlias:\n /r"},
				{"local","local chat","/l <message>\nAlias:\n /l"},
				{"ltoggle","toggle local chat"},
				{"a","admin chat","/a <message>","arcane.mod"},
				{"atoggle","toggle admin chat",null,"arcane.mod"}
			}
	};
	private static final String LWC[][][] = {
			{
				{"cinfo","view info on a protection"},
				{"cprivate","create a private protection","/cprivate [player]"},
				{"Every chests are locked using this by default."},
				{"cdonation","create a donation protection","/cdonation [player]"},
				{"cpublic","create a public protection"},
				{"cmodify","add or remove a player to a protection","/cmodify [-player|player]"},
				{"cremove","remove a protection"}
			},
			{
				{"chopper","changes chest access for hoppers","/chopper [on|off]"},
				{"lwc","official LWC help"}
			}
	};
	private static final String DONOR[][][] = {
			{
				{"slap", "lets you slap a player"},
				{"dynmap hide", "hide your location from the Dynmap"},
				{"dynmap show", "show your location in the Dynmap"}
			}
	};
	private static final String CHATMOD[][][] = {
			{
				{"warn","warns a player","/warn <player> <reason>"},
				{"mute","mutes a player","/mute <player> <reason>"},
				{"tempmute","temporary mutes a player","/tempmute <player> <reason>"},
				{"unmute","unmutes a player","/unmute <player>"},
				{"bminfo","view a player information","/bminfo <player>"},
				{"alts","view a player's alts.","/alts <player>"},
				{"greylist <user>","greylist command","/greylist <player>"}
			}
	};
	private static final String MOD[][][] = {
			{
				{"kick","kick a player","/kick <player> <reason>"},
				{"ban","ban a player" + ChatColor.RED + ChatColor.ITALIC + " like a boss","/ban <player> <reason>"},
				{"tempban","temporary ban a player","/tempban <player> <reason>"},
				{"unban","pardon a player","/unban <player>"},
				{"frz","freeze a player" + ChatColor.RED + ChatColor.ITALIC + " Agent's favorite, Simon hates this!","/frz <player>"},
				{"a","admin chat","/a <message>"},
				{"atoggle","toggle admin chat"}
			},
			{
				{"supervanish","vanish toggle","Alias:\n/sv"},
				{"tp","teleport to a player or location"},
				{"openinv","open a player's inventory","Alias:\n/open\n/inv\noi"},
				{"openender","open a player's Ender chest","Alias:\n/oe"},
				{"coreprotect i","enable the inspection wand","Alias:\n/co i"},
				{"whitelist","whitelist help"},
				{"restart","restart the server","This has slight chance of failing.\nPlease make sure an admin is semi-around in case\nthe restart fails."}
			},
			{
				{"ultraban","super special command" + ChatColor.RED + ChatColor.ITALIC + " in development! Don't use!"},
				{"coreprotect","official CoreProtect help","Alias:\n/co"},
				{"help chatmod", "More moderation help", "Alias:\n/help chatmods"}
			}
	};
}
