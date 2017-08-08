package com.arcaneminecraft.survival;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.arcaneminecraft.api.ArcaneCommons;
import com.arcaneminecraft.api.ColorPalette;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
		
		Greylist gl = new Greylist(this);
		getCommand("greylist").setExecutor(gl);
		getServer().getPluginManager().registerEvents(gl, this);
		
		FindSeen sn = new FindSeen(this);
		getCommand("findplayer").setExecutor(sn);
		getCommand("seen").setExecutor(sn);
		getCommand("seenf").setExecutor(sn);
		
		// This must come before ArcAFK
		getServer().getPluginManager().registerEvents(new PlayerListRole(this), this);
		
		this.afk = new ArcAFK(this);
		getCommand("afk").setExecutor(afk);
		getServer().getPluginManager().registerEvents(afk, this);
		
	}
	
	@Override
	public void onDisable() {
		afk.onDisable();
		for (Player p : getServer().getOnlinePlayers()) {
			p.setPlayerListName(p.getName());
		}
		saveConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// apply
		if (cmd.getName().equalsIgnoreCase("apply")) {
			sender.sendMessage("");
			sender.sendMessage(ArcaneCommons.tagMessage("Apply for build rights here: " + ColorPalette.FOCUS + "https://arcaneminecraft.com/apply/"));
			sender.sendMessage("");
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
			ArrayList<Player> pl = new ArrayList<>();
			
			for (Player p : getServer().getOnlinePlayers()) {
				pl.add(p);
			}
			
			Collections.sort(pl, new Comparator<Player>(){
				@Override
				public int compare(Player a, Player b) {
					return a.getName().compareTo(b.getName());
				}
			});
			
			TextComponent list = new TextComponent(" ");
			list.setColor(ColorPalette.CONTENT);
			
			Iterator<Player> it = pl.iterator();
			
			while (it.hasNext()) {
				Player p = it.next();
				TextComponent tc = new TextComponent(p.getName());
				tc.setColor(this.afk.isAFK(p) ? ColorPalette.CONTENT : ColorPalette.FOCUS);
				tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + p.getName() + " "));
				
				if (this.afk.isAFK(p))
					tc.setItalic(true);
				else 
					tc.setColor(ColorPalette.FOCUS);
				
				list.addExtra(tc);
				
				if (it.hasNext())
					list.addExtra(", ");
			}

			sender.sendMessage(ArcaneCommons.tagMessage("Online players: " + ColorPalette.FOCUS
					+ getServer().getOnlinePlayers().size() + "/" + getServer().getMaxPlayers()));
			sender.spigot().sendMessage(list);
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
