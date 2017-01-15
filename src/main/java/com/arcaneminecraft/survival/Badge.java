package com.arcaneminecraft.survival;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.arcaneminecraft.ArcaneCommons;

public class Badge implements CommandExecutor {
	private final File tagFile;
	private final FileConfiguration tc;
    /* Excerpt

tags:
  _NickV: '&c[Admin]'
  bbycake: '&9[Mod]'
  Morios: '&C[Admin]'
  Champatriot: '&9[Mod]'
  Melium: '&c[Admin]'
  Agentred100: '&c[Admin]'
  jugglingman456: '&c[Admin]'
  Mozos: '&3[Donor]'
  Filly: '&3[Donor]'

     */
	private final ArcaneSurvival plugin;
	
	private final Set<UUID> badgeOn = new HashSet<>();
	private final Map<UUID, String> badgeTag = new HashMap<>();

	Badge(ArcaneSurvival plugin) {
		this.plugin = plugin;
		
		// Storage setup
		tagFile = new File(plugin.getDataFolder(), "badge.yml");

		if (tagFile.exists()) {
			tagFile.getParentFile().mkdirs();
			plugin.saveResource("badge.yml", false);
		}

		tc = new YamlConfiguration();
		try {
			tc.load(tagFile);
			tc.save(tagFile);
		} catch (InvalidConfigurationException | IOException e) {
			e.printStackTrace();
		}
	}

    void save() {
    	try {
			tc.save(tagFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			// toggle badge
			if (sender instanceof Player) {
				toggle((Player) sender);
				return true;
			} else {
				sender.sendMessage(ArcaneCommons.noConsoleMsg());
				return true;
			}
		}
		if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("on")) {
			if (args.length == 1) {
				if (sender instanceof Player)
					toggle((Player)sender, true);
				else {
					sender.sendMessage(ArcaneCommons.noConsoleMsg());
					sender.sendMessage(ArcaneCommons.tag("Or append player name at the end."));
				}
				return true;
			} else if (sender.hasPermission("arcane.admin")) {
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().equalsIgnoreCase(args[1])) {
						toggle(p, true);
						return true;
					}
				}
				sender.sendMessage(ArcaneCommons.tag("Player not online."));
				return true;
			}
		}
		
		if (args[0].equalsIgnoreCase("hide") || args[0].equalsIgnoreCase("off")) {
			if (args.length == 1) {
				if (sender instanceof Player)
					toggle((Player)sender, false);
				else {
					sender.sendMessage(ArcaneCommons.noConsoleMsg());
					sender.sendMessage(ArcaneCommons.tag("Or append player name at the end."));
				}
					return true;
			} else if (sender.hasPermission("arcane.admin")) {
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().equalsIgnoreCase(args[1])) {
						toggle(p, false);
						return true;
					}
				}
				sender.sendMessage(ArcaneCommons.tag("Player not online."));
				return true;
			}
		}
		
		if (args[0].equalsIgnoreCase("set") && sender.hasPermission("arcane.admin")) {
			if (args.length == 1) {
				sender.sendMessage(ArcaneCommons.tag("Not enough arguments. /"+label+" set <tag> [player]"));
				return true;
			}
			if (args.length == 2) {
				if (sender instanceof Player)
					badgeTag.put(((Player)sender).getUniqueId(), ChatColor.translateAlternateColorCodes('&', args[1]));
				else {
					sender.sendMessage(ArcaneCommons.noConsoleMsg());
					sender.sendMessage(ArcaneCommons.tag("Or append player name at the end."));
				}
				return true;
			} else {
				OfflinePlayer p = null;
				for (Player pl : plugin.getServer().getOnlinePlayers()) {
					if (pl.getName().equalsIgnoreCase(args[2])) {
						p = pl;
						return true;
					}
				}
				if (p == null)
					p = plugin.getServer().getOfflinePlayer(args[2]);
				
				badgeTag.put((p).getUniqueId(), ChatColor.translateAlternateColorCodes('&', args[1]));
				sender.sendMessage(ArcaneCommons.tag("Tag assigned to " + p.getName() + "."));
				return true;
			}
		}
		
		if (args[0].equalsIgnoreCase("clear") && sender.hasPermission("arcane.admin")) {
			if (args.length == 1) {
				if (sender instanceof Player)
					badgeTag.remove(((Player)sender).getUniqueId());
				else {
					sender.sendMessage(ArcaneCommons.noConsoleMsg());
					sender.sendMessage(ArcaneCommons.tag("Or append player name at the end."));
				}
				return true;
			} else {
				OfflinePlayer p = null;
				for (Player pl : plugin.getServer().getOnlinePlayers()) {
					if (pl.getName().equalsIgnoreCase(args[1])) {
						p = pl;
						return true;
					}
				}
				
				if (p == null)
					p = plugin.getServer().getOfflinePlayer(args[1]);
				
				badgeTag.remove((p).getUniqueId());
				sender.sendMessage(ArcaneCommons.tag("Tag removed from " + p.getName() + "."));
				return true;
			}
		}

		return false;
	}
	
	public boolean isShown(Player p) {
		return badgeOn.contains(p.getUniqueId());
	}
	
	public String getBadge(Player p) {
		String ret = badgeTag.get(p.getUniqueId());
		return ret == null ? "" : ret + ChatColor.RESET + " ";
	}

	private void toggle(Player p) {
		if (!badgeOn.remove(p.getUniqueId())) {
			badgeOn.add(p.getUniqueId());
			p.sendMessage(ArcaneCommons.tag("Your badge is shown."));
			return;
		}
		p.sendMessage(ArcaneCommons.tag("Your badge is hidden."));
	}

	private void toggle(Player p, boolean on) {
		if (on) {
			badgeOn.add(p.getUniqueId());
			p.sendMessage(ArcaneCommons.tag("Your badge is shown."));
		} else {
			badgeOn.remove(p.getUniqueId());
			p.sendMessage(ArcaneCommons.tag("Your badge is hidden."));
		}
	}
}
