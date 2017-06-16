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
		case "donors":
		case "donor":
			if (sender.hasPermission("arcane.donor")) {
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
			{"Rules","https://arcaneminecraft.com/rules/"},
			{"Forum","https://arcaneminecraft.com/forum/"},
			{"Dynmap","https://arcaneminecraft.com/dynmap/"},
			{"Discord","https://arcaneminecraft.com/discord/"},
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
				{"spawn","return to the spawn"},
				{"home","return to your home","/home [name]"},
				{"sethome","set your home","/sethome [name]"},
				{"kill","temporary ends your suffering just to revive you"},
				{"afk","mark yourself as afk"},
				{"pvp","toggle PvP combat"}
			},
			{
				{"tell","message an online player","/tell <player> <message>\nAlias:\n /t\n /message\n /msg\n /m"},
				{"reply","reply to a recently messaged person","/reply <message>\nAlias:\n /r"},
				{"local","local chat","/l <message>\nAlias:\n /l"},
				{"localtoggle","toggle local chat","Alias:\n /ltoggle\n /lt"},
				{"localradius","set local chat sending radius","Alias:\n /lr"},
				{"global","send regular chat (when toggled)","/g <message>\nAlias:\n /g"},
				{"list","lists all online players"}
			},
			{
				{"seen","displays the date a player was last seen","/seen [player]"},
				{"seenf","displays the date a player joined Arcane","/seenf [player]\nAlias:\n /fseen"},
				{"findplayer","displays the date a player joined Arcane","/seenf [player]\nAlias:\n /fplayer\n /fp"},
				{"ping","get a player's ping","/ping [player]"},
				{"tps","checks server ticks per second"},
				{"links","links to arcane sites"},
				{"news","review the news"}
			},
			{
				{"username", "an advanced username command"},
				{"donors", "list of donor commands", "Alias:\n /donor"},
				{"help lwc", "chest/door protection help","More help:\n /lwc"},
				{"help donor", "donor help", "Alias:\n /help donors", "arcane.donor"},
				{"help chatmod", "chat moderator help", "Alias:\n /help chatmods", "arcane.chatmod"},
				{"help mod", "moderator help", "Alias:\n /help mods", "arcane.mod"}
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
				{"dynmap show", "show your location in the Dynmap"},
				{"badge", "your tag management"}
			}
	};
	private static final String CHATMOD[][][] = {
			{
				{"warn","warns a player","/warn <player> <reason>"},
				{"kick","kick a player","/kick <player> <reason>"},
				{"mute","mutes a player","/mute <player> <reason>"},
				{"tempmute","temporary mutes a player","/tempmute <player> <reason>"},
				{"unmute","unmutes a player","/unmute <player>"},
				{"greylist <user>","greylist command","/greylist <player>"},
				{"newsmod","set or clear news","/newsmod (set|clear) [<news...>]"},
			},
			{
				{"bminfo","view a player information","/bminfo <player>"},
				{"alts","view a player's alts.","/alts <player>"}
			}
	};
	private static final String MOD[][][] = {
			{
				{"tp","teleport to a player or location"},
				{"frz","freeze a player" + ColorPalette.POSITIVE + ChatColor.ITALIC + " Agent's favorite," + ColorPalette.NEGATIVE + ChatColor.ITALIC + " Simon hates this!","/frz <player>"},
				{"ban","ban a player" + ColorPalette.NEGATIVE + ChatColor.ITALIC + " like a boss","/ban <player> <reason>"},
				{"tempban","temporary ban a player","/tempban <player> <reason>"},
				{"unban","pardon a player","/unban <player>"},
				{"supervanish","vanish toggle","Alias:\n/sv"},
				{"coreprotect","official CoreProtect help","Alias:\n /co"}
			},
			{
				{"a","admin chat","/a <message>"},
				{"atoggle","toggle admin chat"},
				{"openinv","open a player's inventory","Alias:\n /open\n /inv\n /oi"},
				{"openender","open a player's Ender chest","Alias:\n /oe"},
				{"whitelist","whitelist help"},
				{"restart","restart the server"},
				{"ultraban","super special command" + ColorPalette.NEGATIVE + ChatColor.ITALIC + " in development! Don't use!"}
			},
			{
				{"help chatmod", "More moderation help", "Alias:\n/help chatmods"}
			}
	};
}
