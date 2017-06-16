package com.arcaneminecraft.survival;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.arcaneminecraft.ArcaneCommons;
import com.arcaneminecraft.ColorPalette;

public final class ArcaneSurvival extends JavaPlugin {
	private ArcAFK afk;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		HelpLink hl = new HelpLink();
		getCommand("help").setExecutor(hl);
		getCommand("link").setExecutor(hl);
		
		PlayerJoin pj = new PlayerJoin(this);
		getCommand("news").setExecutor(pj);
		getServer().getPluginManager().registerEvents(pj, this);
		
		FindSeen sn = new FindSeen(this);
		getCommand("findplayer").setExecutor(sn);
		getCommand("seen").setExecutor(sn);
		getCommand("seenf").setExecutor(sn);
		
		this.afk = new ArcAFK(this);
		getCommand("afk").setExecutor(afk);
		getServer().getPluginManager().registerEvents(afk, this);
		
	}
	
	@Override
	public void onDisable() {
		saveConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// Informational command
		if (cmd.getName().equalsIgnoreCase("arcanesurvival")) {
			sender.sendMessage(ArcaneCommons.tagMessage(this.getDescription().getFullName()));
			// Build Author array
			StringBuilder authors = new StringBuilder();
			Object[] alist = this.getDescription().getAuthors().toArray();
			for (int i = 0; i < alist.length-1; i++) {
				authors.append(alist[i]).append(", ");
			}
			authors.append("and ").append(alist[alist.length-1]);
			
			sender.sendMessage(ColorPalette.CONTENT + " Written with <3 by " + authors + ".");
			return true;
		}
		
		// apply
		if (cmd.getName().equalsIgnoreCase("apply")) {
			sender.sendMessage("");
			sender.sendMessage(ArcaneCommons.tagMessage("Apply for build rights here: " + ColorPalette.FOCUS + "https://arcaneminecraft.com/apply/"));
			sender.sendMessage("");
			return true;
		}
		
		// Shows greylist status / greylists players
		if (cmd.getName().equalsIgnoreCase("greylist")) {
			// Moderators will get a different message
			if (sender.hasPermission("arcane.chatmod")) {
				if (args.length == 0) {
					sender.sendMessage(ArcaneCommons.tagMessage("Usage: /greylist <player>..."));
				} else {
					for (String pl : args)
						((Player)sender).performCommand("pex group trusted user add " + pl);
					// Validity responsibility lies on PEx plugin.
				}
				return true;
			}
			
			// if normal player ran it with some parameters
			if (args.length != 0) {
				sender.sendMessage(ArcaneCommons.noPermissionMsg(label,String.join(" ", args)));
				return true;
			}
			
			if (sender.hasPermission("arcane.trusted"))
				sender.sendMessage(ArcaneCommons.tagMessage("You are on the greylist!"));
			
			else {
				sender.sendMessage(ArcaneCommons.tagMessage("You are " + ColorPalette.NEGATIVE + "not" + ColorPalette.CONTENT + " on the greylist!"));
				sender.sendMessage(ArcaneCommons.tagMessage("Apply for greylist using the /apply command, then talk with a staff member to become greylisted."));
			}

			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("kill")) {
			if (args.length == 0) {
				getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:kill" + (sender instanceof Player ? " " + ((Player)sender).getUniqueId() : ""));
				return true;
			}
			// For selected kill to go through, players will need Minecraft's kill permission in the end.
			if (sender instanceof Player) ((Player)sender).performCommand("minecraft:kill " + args[0]);
			if (sender instanceof ConsoleCommandSender) getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:kill "+ args[0]);
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("list")) {
			ArrayList<String> pl = new ArrayList<>();
			for (Player p : getServer().getOnlinePlayers()) {
				if (afk.isAFK(p)) pl.add( p.getDisplayName());
				else pl.add(ColorPalette.FOCUS + p.getDisplayName() +  ColorPalette.CONTENT);
			}

			sender.sendMessage(ArcaneCommons.tagMessage("Online players: " + ColorPalette.FOCUS
					+ getServer().getOnlinePlayers().size() + "/" + getServer().getMaxPlayers()));
			sender.sendMessage(" " + ColorPalette.CONTENT + String.join(", ", pl));
			sender.sendMessage("");

			return true;

		}

		if (cmd.getName().equalsIgnoreCase("ping")) {
			Player p2ping;
			if (args.length != 0) {
				p2ping = getServer().getPlayer(args[0]);
				if (p2ping == null) {
					sender.sendMessage(ArcaneCommons.tag("Ping", "'" + args[0] + "' is not online."));
					return true;
				}
			}
			else {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ArcaneCommons.tag("Ping","Your ping will forever be <1ms."));
					return true;
				}
				p2ping = (Player)sender;
			}

			StringBuilder m;
			if (p2ping == sender)
				m = new StringBuilder("Pong! Your ");
			else
				m = new StringBuilder(p2ping.getDisplayName()).append("'s ");
			m.append("ping: ");
			
			try {
				Object entityPlayer = p2ping.getClass().getMethod("getHandle").invoke(p2ping);
				int ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
				
				sender.sendMessage(ArcaneCommons.tag("Ping", m.append(ColorPalette.FOCUS).append(ping).append("ms").toString()));
				
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				sender.sendMessage("Ping failed to run.");
				e.printStackTrace();
			}
			return true;
		}
		
		// Useful username command
		// "very useful i give a perfect 5/7" -Simon, 2016
		if (cmd.getName().equalsIgnoreCase("username")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You'll always be named " + sender.getName() + ".");
				return true;
			}
			
			String name = ((Player)sender).getDisplayName();

			Random randy = new Random();

			String[] list = {
					"It looks like your username is " + name + ".",
					"Your username is " + name + ".",
					"Your username is not Agentred100.",
					"Username: " + name + ".",
					ColorPalette.NEGATIVE + "[Username] " + ColorPalette.CONTENT + name + ".",
					ColorPalette.HEADING + "[Username]" + ColorPalette.CONTENT + " At the moment, your username is " + name + ".",
					ColorPalette.HEADING + "YOUR USERNAME IS " + ColorPalette.NEGATIVE + name + ".",
					name
			};

			String r = list[randy.nextInt(list.length)];

			sender.sendMessage(ColorPalette.CONTENT + r);
			return true;
		}
		return false;
	}
}
