package com.arcaneminecraft.survival;

import java.text.SimpleDateFormat;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.arcaneminecraft.ArcaneCommons;
import com.arcaneminecraft.ColorPalette;

final class Seen implements CommandExecutor {
	private static final String TAG = "Seen";
	private final ArcaneSurvival plugin;
	private static final String MAN[][] = {
			{"seen","displays the date a player was last seen","/seen [player]"},
			{"seenf","displays the date a player joined Arcane","/seenf [player]\nAlias:\n /fseen"}
	};
	private static final String FTR[] = {""};
	
	Seen(ArcaneSurvival plugin) {
		this.plugin = plugin;
	}
	
	// for /seen, temporary
    public static String getCurrentDTG(long l_time) {
        java.sql.Date date = new java.sql.Date(l_time);
        SimpleDateFormat dtgFormat = new SimpleDateFormat("hh:mm:ss 'on' MMMM dd yyyy");
        return dtgFormat.format(date);
    }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// seen, seenf, fseen
		// true: /seen, false: /seenf
		boolean runSeen = cmd.getName().equalsIgnoreCase("seen");
		String name;
		long seen;
		StringBuilder msg = new StringBuilder(ArcaneCommons.tag(TAG));
		
		// Look for player in question.
		Player target;
		if (args.length == 0) {
			if (runSeen) {
				// TODO: Display Help
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
		String strDte = getCurrentDTG(seen);
		
		if (runSeen) {
			sender.sendMessage(msg.append(name)
					.append(ColorPalette.CONTENT + " was last seen at " + ColorPalette.FOCUS)
					.append(strDte).toString());
			return true;
		}
		
		if (target != null && sender == target)
			name = "You";
		
		sender.sendMessage(msg.append(name)
				.append(ColorPalette.CONTENT + " first logged in at " + ColorPalette.FOCUS)
				.append(strDte).toString());
		return true;
	}
}
