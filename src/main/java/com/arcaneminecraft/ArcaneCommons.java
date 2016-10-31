package com.arcaneminecraft;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ArcaneCommons {
	public String TAG = ChatColor.GOLD + "[Arcane]" + ChatColor.GRAY;
	
	public void setTag(String tag) {
		TAG = ChatColor.GOLD + "[" + tag + "]" + ChatColor.GRAY;
	}
	
	public void getTag() {
		
	}
	
	public String taggedMessage(String message) {
		
		return TAG + message;
	}
	public String noConsoleMessage() {
		return TAG + " This cannot be run on the console.";
	}
	public String noPermissionMessage() {
		return TAG + " You do not have permission to do that.";
	}
	public String noPermissionMessage(String label) {
		return TAG + " You do not have permission to run " + label + ".";
	}
	public String noPermissionMessage(String label, String subcommand) {
		return TAG + " You do not have permission to use " + subcommand + " in " + label + " command.";
	}

	/**
	 * Sends a formatted command list with multiple pages.
	 * 
	 * @param sender - the sender as shown exactly in CommandSender.
	 * @param header - Custom string to use for help heading.
	 * @param LIST - 3-dimensional array: [page][command line][index]
	 * where "command line" contains:
	 * {
	 *     Command (without leading slash),
	 *     Command details,
	 *     Tooltip menu or URL,
	 *     Permission required
	 * }
	 * The first two fields are mandatory. To skip third or fourth index, use null.
	 *  
	 * @param label - Command used to call help. 
	 * @param subcmd - Sub-command used to call help. 
	 * @param page - Page of the help.
	 * @return true all the time to aid in quick return.
	 */
	public static boolean sendCommandMenu(CommandSender sender, String header, String LIST[][][], String label, String subcmd, int page) {
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
				// skip line if the player has no permission
				if (LIST[a][i].length > 3 && sender.hasPermission(LIST[a][i][3]))
					continue;
				
				TextComponent ret = new TextComponent("> ");
				ret.setColor(ChatColor.GRAY);
				if (LIST[a][i].length == 1) {
					// One-index array
					TextComponent c = new TextComponent(ChatColor.GRAY + LIST[a][i][0]);
					ret.addExtra(c);
				} else {
					// Multiple-index array (2 or 3, maybe 4)
					ret.setColor(ChatColor.GRAY);
					TextComponent c = new TextComponent(ChatColor.GOLD + "/" + LIST[a][i][0]
							+ ChatColor.DARK_GRAY + " - "
							+ ChatColor.GRAY + LIST[a][i][1]);
					if (LIST[a][i].length > 2 && LIST[a][i][2] != null && LIST[a][i][2].startsWith("http")) {
						c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
								, LIST[a][i][2]));
					} else {
						c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND
								, "/" + LIST[a][i][0] + " "));
					}
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
	
	/**
	 * Sends a formatted command list in a single page.
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
	 * The first two fields are mandatory. To skip third or fourth index, use null.
	 *  
	 * @param footerData - 1-dimensional array:
	 * {
	 *     Head,
	 *     Description,
	 *     Link
	 * }
	 * The first two fields are mandatory.
	 * 
	 * @return true all the time to aid in quick return.
	 */
	public static boolean sendCommandMenu(CommandSender sender, String header, String LIST[][], String[] footerData) {
		Player p = (Player)sender;
		
		// Heading
		p.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + " ----- "
					+ ChatColor.GOLD + ChatColor.BOLD + header
					+ ChatColor.GRAY + ChatColor.BOLD + " -----");
		
		// Body
		// Iterate through each line
		for (int i = 0; i < LIST.length; i++) {
			TextComponent ret = new TextComponent("> ");
			if (LIST[i].length == 1) {
				// One-index array
				TextComponent c = new TextComponent(ChatColor.GRAY + LIST[i][0]);
				ret.addExtra(c);
			} else {
				// Multiple-index array (2 or 3, maybe 4)
				ret.setColor(ChatColor.GRAY);
				TextComponent c = new TextComponent(ChatColor.GOLD + "/" + LIST[i][0]
						+ ChatColor.DARK_GRAY + " - "
						+ ChatColor.GRAY + LIST[i][1]);
				if (LIST[i].length > 2 && LIST[i][2] != null && LIST[i][2].startsWith("http")) {
					c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
							, LIST[i][2]));
				} else {
					c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND
							, "/" + LIST[i][0] + " "));
				}
				if (LIST[i].length > 2 && LIST[i][2] != null)
					c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
							, new ComponentBuilder(LIST[i][2]).create()));
				ret.addExtra(c);
			}
			p.spigot().sendMessage(ret);
		}
		
		// Footer
		TextComponent ft = new TextComponent("" + ChatColor.GRAY + ChatColor.BOLD + " -- ");
		TextComponent mw = new TextComponent(ChatColor.GOLD + footerData[0] + ChatColor.GRAY + footerData[1]);
		if (footerData.length > 2)
			mw.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL
					, footerData[2]));
		
		ft.addExtra(mw);
		p.spigot().sendMessage(ft);
		return true;
	}
}
