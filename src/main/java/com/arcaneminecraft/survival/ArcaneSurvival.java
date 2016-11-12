package com.arcaneminecraft.survival;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.arcaneminecraft.ArcaneCommons;

public class ArcaneSurvival extends JavaPlugin{
	private static final String RED = ChatColor.RED + "";
	private static final String GRAY = ChatColor.GRAY + "";
	private static final String WHITE = ChatColor.WHITE + "";
	private static final String YELLOW = ChatColor.YELLOW + "";
	private static final String GOLD = ChatColor.GOLD + "";
	private static final String bold = ChatColor.BOLD + "";
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new ArcaneEvents(), this);
	}

	@Override
	public void onDisable() {
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("arcanesurvival")) {
			sender.sendMessage(ArcaneCommons.tag(this.getDescription().getFullName()));
			// Get versions from other plugins as well
			PluginManager plm = getServer().getPluginManager();
			if (plm.isPluginEnabled("ArcaneChatUtils"))
				sender.sendMessage(ArcaneCommons.tag(plm.getPlugin("ArcaneChatUtils").getDescription().getFullName()));
			if (plm.isPluginEnabled("ArcaneDonor"))
				sender.sendMessage(ArcaneCommons.tag(plm.getPlugin("ArcaneDonor").getDescription().getFullName()));
			if (sender.hasPermission("arcane.mod") && plm.isPluginEnabled("ArcaneModeration"))
				sender.sendMessage(ArcaneCommons.tag(plm.getPlugin("ArcaneModeration").getDescription().getFullName()));
			return true;
		}
		
		// All in HelpLink class
		if (cmd.getName().equalsIgnoreCase("help")) {
			if (sender instanceof Player)
				return HelpLink.commandHelp(sender, label, args);
			sender.sendMessage(ArcaneCommons.noConsoleMsg());
			sender.sendMessage("You're a console. you know what to do!");
			return true;
		}
		
		// HelpLink class as well. This is a super-command.
		// links, link, website, map, forum, discord, mumble, donate
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

		// apply
		if (cmd.getName().equalsIgnoreCase("apply")) {
			sender.sendMessage("");
			sender.sendMessage(GOLD + "           Click here to apply for build rights:");
			sender.sendMessage("");
			sender.sendMessage(WHITE + "           https://arcaneminecraft.com/apply/");
			sender.sendMessage("");
			return true;
		}
		
		// Changes gamemode. This is pretty awesome.
		// g0, g1, g2, g3
		if (cmd.getName().equalsIgnoreCase("g0")) {
			if (sender.hasPermission("arcane.admin") || sender.hasPermission("bukkit.command.gamemode") || sender.hasPermission("minecraft.command.gamemode")) {
				return ((Player)sender).performCommand("gamemode " + label.charAt(1));
			} else {
				sender.sendMessage(ArcaneCommons.noPermissionMsg(label));
				return true;
			}
		}

		// Shows greylist status / greylists players
		if (cmd.getName().equalsIgnoreCase("greylist")) {
			// Moderators will get a different message
			if (sender.hasPermission("arcane.chatmod")) {
				if (args.length == 0) {
					sender.sendMessage(ArcaneCommons.tag(" Usage: /greylist <player>..."));
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
				sender.sendMessage(ArcaneCommons.tag("You are on the greylist!"));
			
			else {
				sender.sendMessage(ArcaneCommons.tag("You are " + RED + "not" + GRAY + " on the greylist!"));
				sender.sendMessage(ArcaneCommons.tag("Apply for greylist using the /apply command, then talk with a staff member to become greylisted."));
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

		// seen, seenf, fseen
		if (cmd.getName().equalsIgnoreCase("seen")) {
			if (label.equals("seen")) {
				if (args.length == 1) {
					Player pTarget = getServer().getPlayer(args[0]);
					if (pTarget != null) {
						sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.WHITE
								+ pTarget.getDisplayName() + ChatColor.GOLD + " is currently online.");
						return true;
					}
					@SuppressWarnings("deprecation")
					OfflinePlayer target = this.getServer().getOfflinePlayer(args[0]);
					long lastseen = target.getLastPlayed();
					if (lastseen == 0) {
						sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.WHITE + "'" + args[0]
								+ "'" + (Object) ChatColor.GOLD + " not found.");
						return true;
					}
					String strDte = getCurrentDTG(lastseen);
					sender.sendMessage((Object) ChatColor.GOLD + "Player " + (Object) ChatColor.WHITE + target.getName()
							+ (Object) ChatColor.GOLD + " Last seen: " + (Object) ChatColor.WHITE + strDte);
					return true;
				}
			return true;
			}
			if (args.length == 0) {
				long firstseen = ((Player)sender).getFirstPlayed();
				String strDte = getCurrentDTG(firstseen);
				sender.sendMessage(
						(Object) ChatColor.GOLD + "You first logged in: " + (Object) ChatColor.WHITE + strDte);
				return true;
			}
			if (args.length == 1) {
				Player targeton = this.getServer().getPlayer(args[0]);
				if (targeton == null) {
					@SuppressWarnings("deprecation")
					OfflinePlayer target = this.getServer().getOfflinePlayer(args[0]);
					long firstseen = target.getFirstPlayed();
					if (firstseen == 0) {
						sender.sendMessage((Object) ChatColor.GOLD + "Player " + (Object) ChatColor.WHITE + "'"
								+ args[0] + "'" + (Object) ChatColor.GOLD + " not found.");
						return true;
					}
					String strDte = getCurrentDTG(firstseen);
					sender.sendMessage((Object) ChatColor.GOLD + "Player " + (Object) ChatColor.WHITE + target.getName()
							+ (Object) ChatColor.GOLD + " first logged in: " + (Object) ChatColor.WHITE + strDte);
				} else {
					long firstseen = targeton.getFirstPlayed();
					String strDte = getCurrentDTG(firstseen);
					sender.sendMessage((Object) ChatColor.GOLD + "Player " + (Object) ChatColor.WHITE
							+ targeton.getName() + (Object) ChatColor.GOLD + " first logged in: "
							+ (Object) ChatColor.WHITE + strDte);
				}
				return true;
			}
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
		// Low priority for this; normal for donor, high for mod
		@EventHandler (priority=EventPriority.LOW)
		public void playerJoin(PlayerJoinEvent e) {
			Player p = e.getPlayer();
			// Send Join Message
			PlayerJoin.sendWelcomeMessage(p);
			
			// Send non-greylisted message
			if (p.hasPermission("arcane.new"))
				PlayerJoin.sendUnlistedMessage(p);
			
			// First join message
			if (!p.hasPlayedBefore())
				Bukkit.broadcastMessage(YELLOW + p.getName()
						+ " has joined Arcane for the first time.");
		}
	}
	
	// for /seen, temporary
    public static String getCurrentDTG(long l_time) {
        java.sql.Date date = new java.sql.Date(l_time);
        SimpleDateFormat dtgFormat = new SimpleDateFormat("hh:mm:ss 'on' MMMM dd yyyy");
        return dtgFormat.format(date);
    }

}
