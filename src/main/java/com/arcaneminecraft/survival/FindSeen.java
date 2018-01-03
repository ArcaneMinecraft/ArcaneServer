package com.arcaneminecraft.survival;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.arcaneminecraft.api.ArcaneCommons;
import com.arcaneminecraft.api.ColorPalette;

final class FindSeen implements CommandExecutor {
	private static final String TAG = "Seen";
	private final ArcaneSurvival plugin;
	private static final String MAN[][] = {
			{"findplayer","list players with part of specified name","/findplayer <part of name>\nAlias:\n /fplayer\n /fp"},
			{"seen","displays the date a player was last seen","/seen <player>"},
			{"seenf","displays the date a player joined Arcane","/seenf [player]\nAlias:\n /fseen"}
	};
	private static final String FTR[] = {""};

	FindSeen(ArcaneSurvival plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("findplayer")) {
			if (args.length == 0) {
				sender.sendMessage(ArcaneCommons.tag(TAG, "Usage: /findplayer <part of player name>"));
				return true;
			}

			sender.sendMessage(ArcaneCommons.tag(TAG, "Searching for name matching \"" + ColorPalette.FOCUS + args[0] + ColorPalette.CONTENT + "\" - please wait..."));

			BukkitRunnable s = new BukkitRunnable(){
				@Override
				public void run() {
					ArrayList<String> pl = new ArrayList<>();
					Pattern patternSearch = Pattern.compile(args[0], Pattern.CASE_INSENSITIVE);

					// Match all players
					for (OfflinePlayer o : plugin.getServer().getOfflinePlayers()) {
						// This is less costly than regex.
						if (StringUtils.containsIgnoreCase(o.getName(),args[0])) {
							String n = o.getName();

							// Use regex to highlight matched portion
							Matcher m = patternSearch.matcher(n);
							int i = 0;
							String toAdd = "";

							// Match.find() goes until it goes through and matches the entire string.
							while (m.find()) {
								if (m.group() != "") {
									// This part matched. Highlight it.
									toAdd += ColorPalette.CONTENT + n.substring(i, m.start()) + ColorPalette.FOCUS + n.substring(m.start(), m.end());
									i = m.end();
								}
							}
							toAdd += ColorPalette.CONTENT + n.substring(i, n.length());

							// Add it to the list.
							pl.add(toAdd);
						}
					}

					if (pl.isEmpty()) {
						sender.sendMessage(ArcaneCommons.tag(TAG, "There is no name matching \"" + ColorPalette.FOCUS + args[0] + ColorPalette.CONTENT + "\"."));
						return;
					}

					Collections.sort(pl);

					Iterator<String> ip = pl.iterator();
					StringBuilder send = new StringBuilder(" ").append(ip.next());

					sender.sendMessage(ArcaneCommons.tag(TAG, "Players matching \"" + ColorPalette.FOCUS + args[0] + ColorPalette.CONTENT + "\":"));

					while (ip.hasNext()) {
						send.append(ChatColor.DARK_GRAY).append(", ");
						String p = ip.next();

						if (send.length() + p.length() > 88) {
							sender.sendMessage(send.toString());
							send = new StringBuilder(" ").append(p);
						} else {
							send.append(p);
						}

					}
					sender.sendMessage(send.toString());
					sender.sendMessage("");
				}
			};
			s.runTaskAsynchronously(plugin);
			return true;
		}		
		boolean runSeen = cmd.getName().equalsIgnoreCase("seen");

		// Look for player in question.
		Player target;
		if (args.length == 0) {
			if (runSeen) {
				ArcaneCommons.sendCommandMenu(sender, "Seen Help", MAN, FTR);
				return true;
			} else {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ArcaneCommons.noConsoleMsg());
					return true;
				}

				target = (Player)sender;
			}
		} else {
			target = plugin.getServer().getPlayer(args[0]);
		}

		String name;
		long seen;
		StringBuilder msg = new StringBuilder(ArcaneCommons.tag(TAG));

		// Validate Player
		if (target != null) {
			if (runSeen) {
				if (sender == target)
					name = "You are";
				else
					name = ColorPalette.FOCUS + target.getName() + ColorPalette.CONTENT + " is";

				sender.sendMessage(msg.append(name)
						.append(" currently online.").toString());
				return true;
			}
			seen = target.getFirstPlayed();
			name = ColorPalette.FOCUS + target.getName();
		} else {
			@SuppressWarnings("deprecation") // getOfflinePlayer is deprecated (for no good reason)
			OfflinePlayer oTarget = plugin.getServer().getOfflinePlayer(args[0]);
			seen = runSeen ? oTarget.getLastPlayed() : oTarget.getFirstPlayed();
			if (seen == 0) {
				sender.sendMessage(msg.append("Player " + ColorPalette.FOCUS + "'")
						.append(args[0])
						.append("'" + ColorPalette.CONTENT + " not found.").toString());
				return true;
			}
			name = ColorPalette.FOCUS + oTarget.getName();
		}

		// Human-friendly date parsing
		// Get Time Difference (in milliseconds)
		long diff = (new Date().getTime() - seen)/1000; // to seconds
		
		StringBuilder time = new StringBuilder();
		
		if (diff < 60) {
			// Within a minute
			time.append(ColorPalette.FOCUS.toString())
				.append(diff)
				.append(" second");
			if (diff != 1)
				time.append('s');
			time.append(ColorPalette.CONTENT + " ago.");
		} else if (diff < 3600) {
			// Within an hour
			long m = diff/60;
			time.append(ColorPalette.FOCUS.toString())
				.append(m)
				.append(" minute");
			if (m != 1)
				time.append('s');
			time.append(ColorPalette.CONTENT + " ago.");
		} else if (diff < 86400) {
			// Within a day
			// Within an hour
			long h = diff/3600;
			time.append(ColorPalette.FOCUS.toString())
				.append(h)
				.append(" hour");
			if (h != 1)
				time.append('s');
			time.append(ColorPalette.CONTENT + " ago.");
		} else if (diff < 604800) {
			// Within a week
			// Within an hour
			long d = diff/86400;
			long h = diff/3600%24;
			time.append(ColorPalette.FOCUS.toString())
				.append(d)
				.append(" day");
			if (d != 1)
				time.append('s');
			time.append(ColorPalette.CONTENT + " and ")
				.append(h)
				.append(" hour");
			if (h != 1)
				time.append('s');
			time.append(" ago.");
		} else {
			// Over a week
			Date date = new Date(seen);
			String d = new SimpleDateFormat("yyyy-MM-dd").format(date);
			String t = new SimpleDateFormat("HH:mm z").format(date);
	
			time.append("on " + ColorPalette.FOCUS)
				.append(d)
				.append(ColorPalette.CONTENT + " at ")
				.append(t);
		}

		if (runSeen) {
			sender.sendMessage(msg.append(name)
					.append(ColorPalette.CONTENT + " was last seen " + time.toString()).toString());
			return true;
		}

		if (target != null && sender == target)
			name = "You";

		sender.sendMessage(msg.append(name)
				.append(ColorPalette.CONTENT + " first logged in " + time.toString()).toString());
		return true;
	}
}
