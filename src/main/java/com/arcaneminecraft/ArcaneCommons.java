/**
 * ArcaneCommons Class.
 * This class is to be shared between all the other plugins, favorably with
 * Arcane plugins.
 * @author Simon Chuu (SimonOrJ) 
 */
package com.arcaneminecraft;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public final class ArcaneCommons {
	public static final String TAG = ColorPalette.TAG.toString() + ChatColor.BOLD + "[Arcane]";
	
	/**
	 * Message with a generic tag
	 * @param message - Message to send with the tag. Default color is ColorPalette.CONTENT.
	 * @return String of the message
	 */
	public static String tag(String message) {
		return TAG + " " + ColorPalette.CONTENT + message;
	}
	/**
	 * Message with a generic tag
	 * @param tag - Tag to be formatted (don't insert brackets)
	 * @param message - Message to send. Default color is gray.
	 * @return String of the message
	 */
	public static String tag(String tag, String message) {
		return ColorPalette.TAG.toString() + ChatColor.BOLD + "[" + tag + "] " + ColorPalette.CONTENT + message;
	}

	/**
	 * Generic no permission message
	 * @return
	 */
	public static String noPermissionMsg() {
		return tag("You do not have permission to do that.");
	}
	/**
	 * Generic no permission message containing command.
	 * @param cLabel
	 * @return
	 */
	public static String noPermissionMsg(String cLabel) {
		return tag("You do not have permission to run \"/" + cLabel + "\".");
	}
	/**
	 * Generic no permission message with arguments.
	 * @param cLabel
	 * @param subcommand
	 * @return
	 */
	public static String noPermissionMsg(String cLabel, String subcommand) {
		return tag("You do not have permission to use \""
				+ subcommand + "\" in \"/" + cLabel + "\".");
	}
	
	/**
	 * Returns a "no-console" message
	 * @return
	 */
	public static String noConsoleMsg() {
		return tag("You must be a player.");
	}

	/**
	 * Sends a formatted command list with multiple pages.  This is
	 * unconventional in a way that it doesn't return the string to send to the
	 * player, but sends the strings for you. (since it's a multi-line message).
	 * 
	 * @param sender - the sender as shown exactly in CommandSender.
	 * @param header - Custom string to use for help heading.
	 * @param LIST - 3-dimensional array: [page][command line][index]
	 * <br>where "command line" contains:
	 * <br>{
	 * <br>    Command (without leading slash),
	 * <br>    Command details,
	 * <br>    Tooltip menu or URL,
	 * <br>    Permission required
	 * <br>}
	 * <br>OR
	 * <br>{
	 * <br>    A sentence string
	 * <br>}
	 * <br>The first field is mandatory. To skip third or fourth index, use null.
	 *  
	 * @param label - Command used to call help. 
	 * @param subcmd - Sub-command used to call help. 
	 * @param page - Page of the help.
	 * @return true all the time to aid in quick return.
	 */
	public static boolean sendCommandMenu(CommandSender sender, String header, String LIST[][][], String label, String subcmd, int page) {
		String[] fData = {label, subcmd};
		int a = page - 1;
		String[][] list;
		// Catch out of bounds
		if (a < LIST.length && a >= 0)
			list = LIST[a];
		else
			list = new String[0][0];
		return sendListCommon(sender, header, list, LIST, fData, a, true);
	}
	
	/**
	 * Sends a formatted command list with a single page.  This is
	 * unconventional in a way that it doesn't return the string to send to the
	 * player, but sends the strings for you. (since it's a multi-line message).
	 * 
	 * 
	 * @param sender - the sender as shown exactly in CommandSender.
	 * @param header - Custom string to use for help heading.
	 * @param LIST - 2-dimensional array: [command line][index]
	 * where "command line" contains:
	 * {
	 *     Command (without leading slash),
	 *     Command details,
	 *     Tooltip menu or URL,
	 *     Permission required
	 * }
	 * OR
	 * {
	 *     A sentence string
	 * }
	 * The first two fields are mandatory. To skip third or fourth index, use null.
	 *  
	 * @param footerData - 1-dimensional array:
	 * {
	 *     Head,
	 *     Description,
	 *     Link/Command/Tooltip
	 * }
	 * The first field is mandatory.
	 * 
	 * @return true all the time to aid in quick return.
	 */
	public static boolean sendCommandMenu(CommandSender sender, String header, String LIST[][], String[] footerData) {
		return sendListCommon(sender, header, LIST, null, footerData, 0, true);
	}
	
	/**
	 * Same parameters, but lists "commands" in a list format.
	 * @see sendCommandMenu()
	 */
	public static boolean sendListMenu(CommandSender sender, String header, String LIST[][], String[] footerData) {
		return sendListCommon(sender, header, LIST, null, footerData, 0, false);
	}

	private static boolean sendListCommon(CommandSender sender, String header, String LIST[][], String PGLIST[][][], String fData[], int a, boolean isCommand) {
		
		// Heading
		sender.sendMessage("" + ColorPalette.CONTENT + ChatColor.BOLD + " ----- "
					+ ColorPalette.TAG + ChatColor.BOLD + header
					+ ColorPalette.CONTENT + ChatColor.BOLD + " -----");
		
		// Body
		// Iterate through each line
		for (int i = 0; i < LIST.length; i++) {
			// skip line if the player has no permission
			if (LIST[i].length > 3 && sender.hasPermission(LIST[i][3]))
				continue;
			
			TextComponent ret = new TextComponent("> ");
			ret.setColor(ChatColor.DARK_GRAY);
			
			if (LIST[i].length == 1) {
				// One-index array
				TextComponent c = new TextComponent(ChatColor.GRAY + LIST[i][0]);
				ret.addExtra(c);
			} else {
				// Multiple-index array (2 or 3, maybe 4)
				TextComponent c = new TextComponent(ColorPalette.TAG
						+ (isCommand ? "/" : "")
						+ LIST[i][0]
						+ ChatColor.DARK_GRAY + " - "
						+ ColorPalette.CONTENT + LIST[i][1]);
				if (isCommand) {
					// command list
					c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND
							, "/" + LIST[i][0] + " "));
				} else if (LIST[i][1].startsWith("http")) {
					// If URL in description
					c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
							, LIST[i][1]));
				} else if (LIST[i].length > 2 && LIST[i][1] != null && LIST[i][2].startsWith("http")) {
					// If URL in tooltip
					c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
							, LIST[i][2]));
				}
				
				if (LIST[i].length > 2 && LIST[i][2] != null)
					// If tooltip parameter exists 
					c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
							, new ComponentBuilder(LIST[i][2]).create()));
				ret.addExtra(c);
			}
			if (sender instanceof Player)
				((Player)sender).spigot().sendMessage(ret);
			else
				sender.sendMessage(ret.toPlainText());
		}
		
		// Footer
		TextComponent ft = new TextComponent("" + ChatColor.GRAY + ChatColor.BOLD + " -- ");
		if (PGLIST != null) {
			// footerData: 0=label, 1=subcmd
			ft.addExtra(ColorPalette.TAG + "Pages: ");
			for (int i = 0; i < PGLIST.length; i++) {
				int npg = i + 1;
				// Compose a list of commands
				String pgLs = ChatColor.GRAY + "Page " + npg + ":" + ChatColor.GOLD;
				
				for (String c[] : PGLIST[i])
					pgLs += "\n /" + c[0];
				
				TextComponent pg = new TextComponent("[" + npg + "]");
				if (i == a)
					pg.setColor(ChatColor.DARK_GRAY);
				else
					pg.setColor(ChatColor.GRAY);
				pg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
						, new ComponentBuilder(pgLs).create()));
				pg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND
						, "/" + fData[0] + " " + (fData[1] == null ? "" : fData[1] + " ") + npg));
				ft.addExtra(pg);
				ft.addExtra(" ");
			}
		} else {
			TextComponent mw = new TextComponent(ColorPalette.TAG + fData[0] + ": " + ColorPalette.CONTENT + fData[1]);
			// If second argument is a link
			if (fData[1].startsWith("http"))
				mw.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
						, fData[1]));
			// If has third argument
			if (fData.length > 2) {
				if (fData[2].startsWith("http"))
					mw.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
							, fData[2]));
				else if (fData[2].startsWith("/"))
					mw.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND
							, fData[2]));
				mw.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
						, new ComponentBuilder(fData[2]).create()));
			}
			ft.addExtra(mw);
		}
		
		if (sender instanceof Player)
			((Player)sender).spigot().sendMessage(ft);
		else
			sender.sendMessage(ft.toPlainText());
		return true;
	}
}