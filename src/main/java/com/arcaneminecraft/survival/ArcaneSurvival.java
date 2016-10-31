package com.arcaneminecraft.survival;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.arcaneminecraft.ArcaneCommons;

public class ArcaneSurvival extends JavaPlugin{
	static final String VERSION = "2.0.0-SNAPSHOT";
	
	private static final String RED = ChatColor.RED + "";
	private static final String GRAY = ChatColor.GRAY + "";
	private static final String WHITE = ChatColor.WHITE + "";
	private static final String YELLOW = ChatColor.YELLOW + "";
	private static final String GOLD = ChatColor.GOLD + "";
	private static final String bold = ChatColor.BOLD + "";
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new ArcaneEvents(), this);
		getServer().getLogger().info("ArcaneSurvival has been loaded!");
	}

	@Override
	public void onDisable() {
		getServer().getLogger().info("ArcaneSurvival is disabled.");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("arcanesurvival")) {
			sender.sendMessage(TAG + GRAY + " Version " + VERSION);
			return true;
		}
		
		// All in HelpLink class
		if (cmd.getName().equalsIgnoreCase("help")) {
			if (sender instanceof Player)
				return HelpLink.commandHelp(sender, label, args);
			sender.sendMessage(noConsoleCmd());
			sender.sendMessage("You're a console. you know what to do!");
			return true;
		}
		
		// HelpLink class as well. This is a super-command.
		if (cmd.getName().equalsIgnoreCase("links")) {
			// check String label || catch console
			if (label.equalsIgnoreCase("links") || !(sender instanceof Player))
				return HelpLink.commandLink(sender);
			// Backward compatibility
			if (label.equalsIgnoreCase("mumble"))
				label = "discord";
			if (HelpLink.commandSingleLink(sender, label))
				return true;
			return HelpLink.commandLink(sender);
		}

		if (cmd.getName().equalsIgnoreCase("apply")) {
			sender.sendMessage("");
			sender.sendMessage(GOLD + "           Click here to apply for build rights:");
			sender.sendMessage("");
			sender.sendMessage(WHITE + "           https://arcaneminecraft.com/apply/");
			sender.sendMessage("");
			return true;
		}
		
		// This is pretty awesome
		if (cmd.getName().equalsIgnoreCase("g0")) {
			if (sender.hasPermission("bukkit.command.gamemode") || sender.hasPermission("minecraft.command.gamemode")) {
				return ((Player)sender).performCommand("gamemode " + label.charAt(1));
			} else {
				ArcaneCommons.sendNoPermission(sender);
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("greylist")) {
			// Moderators will get a different message
			if (sender.hasPermission("arcane.chatmod") || sender.hasPermission("arcane.mod")) {
				if (args.length == 0) {
					sender.sendMessage(TAG + " Usage: /greylist <player>...");
				} else {
					for (String pl : args)
						((Player)sender).performCommand("pex group trusted user add " + pl);
					// Validity responsibility lies on PEx plugin.
				}
				return true;
			}
			
			// if normal player ran it with some parameters
			if (args.length != 0) {
				sender.sendMessage(noPermissionCmd(label,String.join(" ", args)));
				return true;
			}
			
			if (sender.hasPermission("arcane.trusted"))
				sender.sendMessage(TAG + "You are on the greylist!");
			
			else {

				sender.sendMessage(TAG + "You are " + RED + "not" + GRAY + " on the greylist!");
				sender.sendMessage(TAG + "Talk with a staff member to become greylisted.");

			}

			return true;
		}

		// TODO: Bring AFK functionality in the house (this plugin)!
		if (cmd.getName().equalsIgnoreCase("list")) {
			StringBuilder players = new StringBuilder();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (players.length() > 0) {
					players.append(", ");
				}
				players.append(player.getDisplayName());
			}

			if (sender instanceof Player) {
				sender.sendMessage(GOLD + " Online players: " + ChatColor.RESET
						+ Bukkit.getServer().getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
				sender.sendMessage(" " + players.toString());
			}
			else
				getServer().getLogger().info(players.toString());

			return true;

		}

		// Surpress /tell
		// TODO: Move this to the messaging plugin
		if(cmd.getName().equalsIgnoreCase("tell")) {
			return (((Player)sender).performCommand("msg " + String.join(" ", args)));
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
			List<String> List = new ArrayList<String>();

			List.add(GRAY + "It looks like your username is " + name + ".");
			List.add(GRAY + "Your username is " + name + ".");
			List.add(GRAY + "Your username is not Agentred100.");
			List.add(GRAY + "Username: " + name + ".");
			List.add(RED + "[Username] " + GRAY + name + ".");
			List.add(GOLD + "[Username]" + GRAY + " At the moment, your username is " + name + ".");
			List.add(GOLD + "YOUR USERNAME IS " + RED + name + ".");
			List.add(GRAY + name);

			String r = List.get(randy.nextInt(List.size()));

			sender.sendMessage(r);
			return true;
		}

		// TODO: what?
		// Real TODO: move this over to SpigotTesting
		if (cmd.getName().equalsIgnoreCase("f") & sender.hasPermission("arcane.f")) {

			Player p1 = (Player) sender;

			Firework fw = (Firework) p1.getWorld().spawn(p1.getLocation(), Firework.class);
			FireworkEffect effect = FireworkEffect.builder().trail(true).flicker(false).withColor(Color.RED)
					.with(Type.BURST).build();
			FireworkMeta fwm = fw.getFireworkMeta();
			fwm.clearEffects();
			fwm.addEffects(effect);

			@SuppressWarnings("unused")
			Field f;

			try {
				f = fwm.getClass().getDeclaredField("power");
			} catch (NoSuchFieldException e) {
				e.printStackTrace();

			}

			fw.setFireworkMeta(fwm);
			return true;

		}

		// TODO: This too goes to SpigotTesting
		if (cmd.getName().equalsIgnoreCase("doge")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("operatordogedogedogedoge...");
				return true;
			}

			if (sender.isOp()) {
				sender.sendMessage(RED + bold + "dogecoins iz teh reals " + ((Player)sender).getExhaustion());
				return true;
			}
			return false;
		}
		return false;
	}
	
	public final class ArcaneEvents implements Listener {
		@EventHandler
		public void newPlayerJoin(PlayerJoinEvent e) {
			Player player = e.getPlayer();
			if (!player.hasPlayedBefore())
				Bukkit.broadcastMessage(YELLOW + player.getName()
						+ " has joined Arcane for the first time.");
		}

		// Should move to the plugin related to moderation
		@EventHandler
		public void diamondWatcher(BlockBreakEvent e) {
			if ((e.getBlock().getY() <= 20)
					&& (e.getBlock().getType() == Material.DIAMOND_ORE)) {
				Player player = e.getPlayer();
				Date date = new Date();

				// TODO: Where did the live announcement code go?
				// TODO: CoreProtect already logs blocks. Is this still necessary?
				logToFile("[" + date.toString() + "] " + player.getName()
						+ " mined diamond ore at " + e.getBlock().getX() + ", "
						+ e.getBlock().getY() + ", " + e.getBlock().getZ() + ".",
						"xraylog");
			}
		}
	}

	public void logToFile(String message, String fileName) {
		try {
			File dataFolder = getDataFolder();
			if (!dataFolder.exists()) {
				dataFolder.mkdir();
			}
			File saveTo = new File(getDataFolder(), fileName + ".txt");
			if (!saveTo.exists()) {
				saveTo.createNewFile();
			}
			FileWriter fw = new FileWriter(saveTo, true);

			PrintWriter pw = new PrintWriter(fw);

			pw.println(message);

			pw.flush();

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
