package com.arcaneminecraft.survival;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.arcaneminecraft.ArcaneCommons;
import com.arcaneminecraft.ColorPalette;

final class HelpLink implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// All in HelpLink class
		if (cmd.getName().equalsIgnoreCase("help")) {
			return commandHelp(sender, label, args);
		}
		
		// HelpLink class as well. This is a super-command.
		// links, link, website, map, forum, discord, mumble, donate
		if (cmd.getName().equalsIgnoreCase("links")) {
			return HelpLink.commandLink(sender);
		}
		
		return false;
	}
	
	// Main function; filter it
	private boolean commandHelp(CommandSender sender, String label, String args[]) {
		// Default
		if (args.length == 0) {
			return ArcaneCommons.sendCommandMenu(sender, "General Help", HELP, label, null, 1);
        }
		
		// if numeric
		if (StringUtils.isNumeric(args[0])) {
			return ArcaneCommons.sendCommandMenu(sender, "General Help", HELP, label, null, Integer.parseInt(args[0]));
		}
		
		// if has second argument (must be numeric)
		int page = 1;
		if (args.length > 1 && StringUtils.isNumeric(args[1]))
			page = Integer.parseInt(args[1]);
		
		String subcmd = args[0].toLowerCase();
		switch (subcmd) {
		case "lwc":
			return ArcaneCommons.sendCommandMenu(sender, "LWC Help", LWC, label, subcmd, page);
		case "message":
		case "msg":
			return ArcaneCommons.sendCommandMenu(sender, "Messaging Help", MESSAGE, label, subcmd, page);
		case "donors":
		case "donor":
			if (sender.hasPermission("arcane.mod")) {
				return ArcaneCommons.sendCommandMenu(sender, "Donor Help", DONOR, label, subcmd, page);
			}
			sender.sendMessage(ArcaneCommons.noPermissionMsg(label, subcmd));
			return true;
		case "chatmods":
		case "chatmod":
			if (sender.hasPermission("arcane.mod") || sender.hasPermission("arcane.chatmod")) {
				return ArcaneCommons.sendCommandMenu(sender, "Chatmod Help", CHATMOD, label, subcmd, page);
			}
			sender.sendMessage(ArcaneCommons.noPermissionMsg(label, subcmd));
			return true;
		case "mods":
		case "mod":
			if (sender.hasPermission("arcane.mod")) {
				return ArcaneCommons.sendCommandMenu(sender, "Moderator Help", MOD, label, subcmd, page);
			}
			sender.sendMessage(ArcaneCommons.noPermissionMsg(label, subcmd));
			return true;
		}
		return false;
	}
	
	// Function for getting links
	static boolean commandLink(CommandSender sender) {
		String footerData[] = {"Main Website", "https://arcaneminecraft.com/"};
		return ArcaneCommons.sendListMenu(sender, "Arcane Links", LINK, footerData);
	}
	
	private static final String LINK[][] = {
			{"Dynmap","https://arcaneminecraft.com/dynmap/"},
			{"Discord","https://arcaneminecraft.com/discord/"},
			{"Forum","https://arcaneminecraft.com/forum/"},
			{"Donation","https://arcaneminecraft.com/donate/"}
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
				{"ban","ban a player" + ColorPalette.NEGATIVE + ChatColor.ITALIC + " like a boss","/ban <player> <reason>"},
				{"tempban","temporary ban a player","/tempban <player> <reason>"},
				{"unban","pardon a player","/unban <player>"},
				{"frz","freeze a player" + ColorPalette.POSITIVE + ChatColor.ITALIC + " Agent's favorite," + ColorPalette.NEGATIVE + ChatColor.ITALIC + " Simon hates this!","/frz <player>"},
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
				{"ultraban","super special command" + ColorPalette.NEGATIVE + ChatColor.ITALIC + " in development! Don't use!"},
				{"coreprotect","official CoreProtect help","Alias:\n/co"},
				{"help chatmod", "More moderation help", "Alias:\n/help chatmods"}
			}
	};
}