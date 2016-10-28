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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
//import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ArcaneSurvival extends JavaPlugin{
	private static final String VERSION = "2.0.0-SNAPSHOT";
	
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
		Player p = (Player) sender;

		String yt = ChatColor.GOLD + "[From: ytorgonak] " + ChatColor.GRAY;
		String ww = ChatColor.BLUE + "//";
		String red = ChatColor.RED + "";
		String gray = ChatColor.GRAY + "";
		String white = ChatColor.WHITE + "";
		String green = ChatColor.GREEN + "";
		String yellow = ChatColor.YELLOW + "";
		String it = ChatColor.ITALIC + "";
		String gold = ChatColor.GOLD + "";
		String bold = ChatColor.BOLD + "";


		// DONORS
		// TODO: The donor commands should be made into their own plugin(s)
		if (cmd.getName().equalsIgnoreCase("sharpshootingace")) {
			if (args.length == 0) {

				p.sendMessage(gold + "---- SharpshootingAce Plugin 1.0.0 ----");
				p.sendMessage(gold
						+ "[\\]"
						+ gray
						+ " This is the menu for SharpPlug. The command are as follows:");
				p.sendMessage(gold + "[\\] " + green + "/sharp enchant " + gray
						+ "List the enchantments for an item");
				p.sendMessage(gold + "[\\] " + green + "/sharp info " + gray
						+ "View your information");
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("enchant")) {
					if (p.getItemInHand().getEnchantments().size() > 0) {
						p.sendMessage(gold
								+ "---- SharpPlug Enchantment Lookup ---- ");
						p.sendMessage(gold + "[\\] " + green + "Item: " + white
								+ p.getItemInHand().getType());
						p.sendMessage(gold
								+ "[\\] "
								+ green
								+ "Enchantment: "
								+ white
								+ p.getItemInHand().getEnchantments()
										.toString());
					} else {
						p.sendMessage(gold + "[\\] "
								+ "SharpPlug Error Report: " + ChatColor.RED
								+ "1 Error(s) detected!");
						p.sendMessage(gold + "[\\] Code: " + gray
								+ "ItemNotEnchanted=true");
						p.sendMessage(gold + "[\\] English: " + gray
								+ "That item is not enchanted!");
					}
				} else if (args[0].equalsIgnoreCase("info")) {

					p.sendMessage(gold + "---- SharpPlug Player Lookup ----");
					p.sendMessage(gold + "[\\] " + green + "Username: " + gray
							+ p.getDisplayName());
					p.sendMessage(gold + "[\\] " + green + "Health: " + gray
							+ p.getHealth());
					p.sendMessage(gold + "[\\] " + green + "Experience: "
							+ gray + p.getExp());
				}
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("ytorgonak")) {
			if (args.length == 0) {
				p.sendMessage(ChatColor.BLUE + "---- " + ChatColor.GRAY
						+ "ytorgonakPlugin 2.0" + ChatColor.BLUE + " ----");
				p.sendMessage(ww
						+ ChatColor.GRAY
						+ " This is the menu for the ytorgonak plugin - Version 2.0");
				p.sendMessage(ww + ChatColor.GRAY
						+ " /ytor id - Let ytorgonak identify your item");
				p.sendMessage(ww + ChatColor.GRAY
						+ " /ytor loc - Let ytorgonak give you your location");
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("id")) {
					p.sendMessage(yt + "That's a(n) "
							+ p.getItemInHand().getType() + " - "
							+ p.getItemInHand().getAmount() + " of 'em!");
				} else if (args[0].equalsIgnoreCase("loc")) {
					int x = p.getLocation().getBlockX();
					int y = p.getLocation().getBlockY();
					int z = p.getLocation().getBlockZ();

					p.sendMessage(yt + "Hey! You're located at " + x + "x, "
							+ y + "y, " + z + "z!");
				}
				return true;
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("dclem")) {
			Random random = new Random();
			ArrayList<String> smallList = new ArrayList<String>();

			smallList.add("Build a house for a zombie! Zombies are human too.");
			smallList.add("Build a camp in the forest! AKA \"Going Canadian\"?");
			smallList.add("Build an entrance to a mine! It's half of the game's name, better make it pretty.");
			smallList.add("Build a garden! Remember that opium comes from poppies.");
			smallList.add("Build a graveyard! 2spooky4me?");
			smallList.add(
					"Build a time capsule by burying a chest somewhere! Don't forget the \"ayy lmao\" paper in there.");
			smallList.add("Build a fountain! Bonus point if it doesn't look phallic.");
			smallList.add("Build a tent! Camping like a boss.");
			smallList.add("Build a dirthouse. Gotta start somewhere, amiright?");

			String smallrandom = smallList.get(random.nextInt(smallList.size() - 1));

			ArrayList<String> bigList = new ArrayList<String>();

			bigList.add("Build a lighthouse! No evil shall escape your sight!");
			bigList.add("Build a ship! Agentred will be so jealous of your skills.");
			bigList.add("Build a PvP arena! Watching your friends murder eachother in your own arena is priceless.");
			bigList.add("Build a monument to DClem's glory! Then give him diamonds.");
			bigList.add("Build a treehouse! \"Kids NExt Door\", anyone?");
			bigList.add(
					"Build a watchtower! Wooden if you live in the forest, stone if you live in the mountains, clay if you live in the sea.");
			bigList.add("Build an inn! Don't let any creepers in.");
			bigList.add("Build an Egyptian themed build! Then walk like an Egyptian.");
			bigList.add("Build an Asian themed build! Senpai will surely notice you after that!");
			bigList.add("Build a 1/1 scale replica of a real or fictional build! No inspiration? Just copy.");

			String bigrandom = bigList.get(random.nextInt(bigList.size() - 1));

			ArrayList<String> redList = new ArrayList<String>();

			redList.add("Build a sorting system for your mine! Then give your diamonds to DClem.");
			redList.add("Build a nano farm! Sometimes, smaller can be better.");
			redList.add("Build a 3x3 secret door to a secret part of your base! So fancy!");
			redList.add("Build a \"panic room\" in your base, fully equipped with what you need to survive an apocalypse!");
			redList.add("Build a nether hub with portals to 3 different biomes! A whole new world!");
			redList.add(
					"Build a farm with everything that is farmable! Ain't nobody got time to travel for cacti or vines, amiright?");
			redList.add("Build an armor for mining! Dig it hard, but use blast protection ;D");
			redList.add("Build a fully equipped enchant-station! It's a kind of magic.");
			redList.add("Build a wool farm with every wool type! Then paint with all the colors of the w...wool.");

			String redrandom = redList.get(random.nextInt(bigList.size() - 1));

			ArrayList<String> comList = new ArrayList<String>();

			comList.add(
					"Build a portal and put it on one of the Nether Highways! Orange Highway is the best, just sayin'.");
			comList.add("Build a shop in the Market Ravine! And then shop 'til your drop... in the Ravine.");
			comList.add(
					"Build a road linking up your base to one of the Overworld Roads! A journey of a thousand miles begins with a single step.");
			comList.add("Check Spawn for any eventual griefs! Arcane's equivalent of \"citizen's arrest\"?");
			comList.add(
					"Help a new player find his way around Spawn and Arcane! We were all noobs in the beginning. Except for Agentred, of course.");
			comList.add("Give gifts to 3 players through the mailboxes at Spawn! So nice of you <3");
			comList.add(
					"Open a RolePlay thread on the forum and invent a lore for your village/base/settlement! The People's Republic of Taiga will crush you anyway.");
			comList.add("Fill the gear and food chests at spawn! Nobody likes to die on his/her first night.");
			comList.add("Ask around if anyone wants help with building or ressource gathering, and then help them!");

			String comrandom = comList.get(random.nextInt(bigList.size() - 1));

			String intro = gray + bold + ">" + green + bold + "> ";


			if (args.length == 0) {

				p.sendMessage(intro + gold + bold + "DClem's Anti-Boredom Build Ideas!" + red + bold + " V1.0");
				p.sendMessage(
						intro + gray + "For " + yellow + "Small Building Ideas," + gray + " Type /dclem " + "small");
				p.sendMessage(intro + gray + "For " + yellow + "Big Building Ideas," + gray + " Type /dclem " + "big");
				p.sendMessage(intro + gray + "For " + yellow + "Redstone Building Ideas," + gray + " Type /dclem "
						+ "redstone");
				p.sendMessage(
						intro + gray + "For " + yellow + "Community Building Ideas," + gray + " Type /dclem community");
			}

			if (args.length == 1) {

				switch (args[0].toLowerCase()) {
				case "small":
					p.sendMessage(intro + gold + "DClem's Build Idea: " + gray + smallrandom);
					break;

				case "big":
					p.sendMessage(intro + gold + "DClem's Build Idea: " + gray + bigrandom);
					break;

				case "redstone":
					p.sendMessage(intro + gold + "DClem's Build Idea: " + gray + redrandom);
					break;

				case "community":
					p.sendMessage(intro + gold + "DClem's Build Idea: " + gray + comrandom);
					break;
				}
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("bbycake")) {

			Player player = p.getPlayer();

			if (player.hasPermission("arcane.bbycake")) {

				p.sendMessage("You are bbycake.");

			} else {

				p.sendMessage("You are not bbycake.");

			}
			return true;
		}

		
		// END DONORS

		
		//TODO need to resolve /arcane help and /help
		if (cmd.getName().equalsIgnoreCase("arcanesurvival")) {
			if (args.length == 0) {
				p.sendMessage(ChatColor.GOLD + "[ArcaneSurvival] "
						+ ChatColor.WHITE + "Version " + VERSION);
				return true;
			}

			if (args.length == 1) {

				if (args[0].equalsIgnoreCase("help")
						|| args[0].equalsIgnoreCase("?")) {

					p.sendMessage(gold + "------- Arcane Survival --------");
					p.sendMessage(gold + "> " + yellow + "/spawn " + white
							+ "- Return to the spawn");
					p.sendMessage(gold + "> " + yellow + "/home " + white
							+ "- Takes you to your home");
					p.sendMessage(gold + "> " + yellow + "/pvp " + white
							+ "- Toggle PvP combat");
					p.sendMessage(gold + "> " + yellow + "/help lwc " + white
							+ "- Chest protection help");
					p.sendMessage(gold + "> " + yellow + "/help ignore "
							+ white + "- Ignore help");
					p.sendMessage(gold + "> " + yellow + "/seen " + white
							+ "- Seen commands");

				}

			} // if args.length == 1

			
			//The message commands were removed as they exist in another plugin

			return true;
		}
		
		// Surpress /tell
		if(cmd.getName().equalsIgnoreCase("tell")) {
			return (((Player)sender).performCommand("msg " + String.join(" ", args)));
		}
		

		if (cmd.getName().equalsIgnoreCase("list")) {

			StringBuilder players = new StringBuilder();

			for (Player player : Bukkit.getOnlinePlayers()) {
				if (players.length() > 0) {
					players.append(", ");
				}

				players.append(player.getDisplayName());
			}

			if(sender instanceof Player) {
			sender.sendMessage(gold + " Online players: " + ChatColor.RESET
					+ Bukkit.getServer().getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
			sender.sendMessage(" " + players.toString());
			}
			else
				getServer().getLogger().info(players.toString());

			return true;

		}

		if (!(sender instanceof Player)) {
			getServer().getLogger().info("You must be a player");
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("greylist") || (cmd.getName().equalsIgnoreCase("graylist"))) {

			if (p.hasPermission("arcane.trusted"))

				p.sendMessage(ChatColor.GOLD + "[ArcaneSurvival] " + ChatColor.WHITE + "You are on the greylist!");

			else {

				p.sendMessage(
						gold + "[ArcaneSurvival] " + white + "You are " + red + "not" + white + " on the greylist!");
				p.sendMessage(gold + "[ArcaneSurvival] " + white + "Talk with a staff member to become greylisted.");

			}

			return true;
		}

		if (cmd.getName().equalsIgnoreCase("map")) {

			p.sendMessage("");
			p.sendMessage(gold + "         Click here to view our Dynmap:");
			p.sendMessage("");
			p.sendMessage(white + "     http://arcaneminecraft.com/dynmap");
			p.sendMessage("");
			return true;

		}

		// Useful username command

		if (cmd.getName().equalsIgnoreCase("username")) {

			Random randy = new Random();
			List<String> List = new ArrayList<String>();

			List.add(gray + "It looks like your username is " + p.getDisplayName() + ".");
			List.add(gray + "Your username is " + p.getDisplayName() + ".");
			List.add(gray + "Your username is not Agentred100.");
			List.add(gray + "Username: " + p.getDisplayName() + ".");
			List.add(red + "[Username] " + gray + p.getDisplayName() + ".");
			List.add(gold + "[Username]" + gray + " At the moment, your username is " + p.getDisplayName() + ".");
			List.add(gold + "YOUR USERNAME IS " + red + p.getDisplayName() + ".");
			List.add(p.getDisplayName());

			String r = List.get(randy.nextInt(List.size()));

			p.sendMessage(r);
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("apply")) {

			p.sendMessage("");
			p.sendMessage(gold + "           Click here to apply for build rights:");
			p.sendMessage("");
			p.sendMessage(white + "           http://arcaneminecraft.com/apply");
			p.sendMessage("");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("mumble") || cmd.getName().equalsIgnoreCase("discord")) {

			p.sendMessage("");
			p.sendMessage(gold + "          Here is our discord invite link:");
			p.sendMessage("");
			p.sendMessage(white + "        http://arcaneminecraft.com/discord");
			p.sendMessage("");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("forum")) {

			p.sendMessage("");
			p.sendMessage(gold + "           Click here to visit our forum:");
			p.sendMessage("");
			p.sendMessage(white + "       http://arcaneminecraft.com/forum");
			p.sendMessage("");
			return true;
		}

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

		if (cmd.getName().equalsIgnoreCase("g0")) {
			if (p.hasPermission("bukkit.command.gamemode") || p.hasPermission("minecraft.command.gamemode")) {
				return p.performCommand("gamemode " + label.charAt(1));
			}
			return false;
		}

		if (cmd.getName().equalsIgnoreCase("donate")) {

			p.sendMessage("");
			p.sendMessage(gold + "              To donate to Arcane, go here:");
			p.sendMessage("");
			p.sendMessage(white + "           http://arcaneminecraft.com/donate");
			p.sendMessage("");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("doge") & p.isOp()) {

			p.sendMessage(red + bold + "dogecoins iz teh reals " + p.getExhaustion());
			return true;
		}

		/*
		 * if ((cmd.getName().equalsIgnoreCase("imposter") ||
		 * cmd.getName().equalsIgnoreCase("imp")) &&
		 * sender.hasPermission("bukkit.broadcast.admin")){
		 * 
		 * //p.sendMessage(ChatColor.YELLOW + "Fake chat sent."); //}
		 * 
		 * int i; if(args.length >= 2){
		 * 
		 * String message = ""; for (i = 1; i < args.length; i++) { message +=
		 * args[i] + " "; }
		 * 
		 * Bukkit.broadcastMessage("<" + args[0] + "> " + message);
		 * 
		 * } return true; }
		 */
		
		// This portion by Simon
		if (cmd.getName().equalsIgnoreCase("links")) {
			p.sendMessage(gray + bold + " ----- " + gold + bold + "Arcane Links" + gray + bold + " ----- ");
			p.sendMessage(gray + "> " + gold + "/Map" + gray + "- http://arcaneminecraft.com/dynmap/");
			p.sendMessage(gray + "> " + gold + "/Forum" + gray + "- http://arcaneminecraft.com/forum/");
			p.sendMessage(gray + "> " + gold + "/Discord" + gray + "- http://arcaneminecraft.com/discord/");
			p.sendMessage(gray + "> Main website is http://arcaneminecraft.com/ .");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("help")) {
			if (args.length == 0 || args[0].equalsIgnoreCase("1")) {
				p.sendMessage(gray + bold + " ----- " + gold + bold + "Arcane Help 1" + gray + bold + " ----- ");
				p.sendMessage(gray + "> " + gold + "/help " + gray + "- show this page");
				p.sendMessage(gray + "> " + gold + "/spawn " + gray + "- return to the spawn");
				p.sendMessage(gray + "> " + gold + "/home " + gray + "- takes you to your home");
				p.sendMessage(gray + "> " + gold + "/sethome " + gray + "- set your home");
				p.sendMessage(gray + "> " + gold + "/pvp " + gray + "- toggle PvP combat");
				p.sendMessage(gray + "> " + gold + "/seen " + gray + "- seen commands");
				p.sendMessage(gray + "> " + gray + "Next page: /help 2");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("2")) {
				p.sendMessage(gray + bold + " ----- " + gold + bold + "Arcane Help 2" + gray + bold + " ----- ");
				p.sendMessage(gray + "> " + gold + "/links " + gray + "- links to arcane sites");
				p.sendMessage(gray + "> " + gold + "/username " + gray + "- an advanced username command");
				p.sendMessage(gray + "> " + gold + "/help lwc " + gray + "- chest protection help");
				p.sendMessage(gray + "> " + gold + "/help msg " + gray + "- message-related help");
				p.sendMessage(gray + "> " + gold + "/help donor " + gray + "- donor commands");
				if (p.hasPermission("arcane.chatmod") || p.hasPermission("arcane.mod")) {
					if (p.hasPermission("arcane.mod"))
						p.sendMessage(gray + "> " + gold + "/help mods " + gray + "- moderator commands");
					p.sendMessage(gray + "> " + gold + "/help chatmods " + gray + "- chatmod commands");
				}
				return true;
			}
			
			if (args[0].equalsIgnoreCase("lwc")) {
				p.sendMessage(gray + bold + " ----- " + gold + bold + "Help LWC" + gray + bold + " ----- ");
				p.sendMessage(gray + "> " + gold + "/cprivate " + gray + "- create a private protection");
				p.sendMessage(gray + "> " + gold + "/cdonation " + gray + "- create a public protection");
				p.sendMessage(gray + "> " + gold + "/cpublic " + gray + "- create a public protection");
				p.sendMessage(gray + "> " + gold + "/cmodify " + gray + "- add a player to a protection");
				p.sendMessage(gray + "> " + gold + "/cremove " + gray + "- remove a protection");
				p.sendMessage(gray + "> " + gold + "/chopper on" + gray + " - grant a chest hopper access");
				p.sendMessage(gray + "> " + gold + "/cinfo " + gray + "- view info on a protection");
				p.sendMessage(gray + "> Chests you place will lock by default.");
				p.sendMessage(gray + "> View full LWC commands by typing /lwc.");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("msg")) {
				p.sendMessage(gray + bold + " ----- " + gold + bold + "Help Message" + gray + bold + " ----- ");
				p.sendMessage(gray + "> " + gold + "/msg " + gray + "- message an online player");
				p.sendMessage(gray + "> Shorthand: /m");
				p.sendMessage(gray + "> " + gold + "/reply " + gray + "- reply to a recently messaged person");
				p.sendMessage(gray + "> Shorthand: /r");
				p.sendMessage(gray + "> " + gold + "/local " + gray + "- local chat ");
				p.sendMessage(gray + "> Shorthand: /l, view full commands by typing /local -h.");
				if (p.hasPermission("arcane.mod")||p.hasPermission("arcane.chatmod")) {
					p.sendMessage(gray + "> " + gold + "/a " + gray + "- staff chat ");
					p.sendMessage(gray + "> " + gold + "/atoggle" + gray + " - staff chat toggle");
				}
				return true;
			}
			
			if(args[0].equalsIgnoreCase("donor")) {
				p.sendMessage(gray + bold + " ----- " + gold + bold + "Help Donor" + gray + bold + " ----- ");
				if (sender.hasPermission("arcane.homes")) {
					p.sendMessage(gray + "> " + gold + "/sethome <name> " + gray + "- set a named home");
					p.sendMessage(gray + "> " + gold + "/home <name> " + gray + "- takes you to the named home");
				}
				if (sender.hasPermission("arcane.donor")) {
					p.sendMessage(gray + "> " + gold + "/slap " + gray + "- lets you slap a player");
					p.sendMessage(gray + "> " + gold + "/dynmap hide " + gray + "- hide your location from the Dynmap");
					p.sendMessage(gray + "> " + gold + "/dynmap show " + gray + "- show your location in the Dynmap");
				}
				p.sendMessage(gray + "> Current special donor commands are /bbycake, /dclem, /sharpshootingace, and /ytorgonak.");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("chatmods") && (sender.hasPermission("arcane.mod") || p.hasPermission("arcane.chatmod"))) {
				if (args.length == 1 || args[1].equalsIgnoreCase("1")) {
					p.sendMessage(gray + bold + " ----- " + gold + bold + "Help Chatmods" + gray + bold + " ----- ");
					p.sendMessage(gray + "> " + gold + "/warn " + gray + "- warns a player");
					p.sendMessage(gray + "> " + gold + "/mute " + gray + "- mutes a player");
					p.sendMessage(gray + "> " + gold + "/tempmute " + gray + "- temporary mutes a player");
					p.sendMessage(gray + "> " + gold + "/unmute " + gray + "- unmutes a player");
					p.sendMessage(gray + "> " + gold + "/bminfo " + gray + "- view a player information");
					p.sendMessage(gray + "> " + gold + "/alts " + gray + "- view a player's alts.");
					p.sendMessage(gray + "> " + gold + "/pex group trusted user add <user> " + gray + "- greylist command");
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("mods") && sender.hasPermission("arcane.mod")) {
				if (args.length == 1 || args[1].equalsIgnoreCase("1")) {
					p.sendMessage(gray + bold + " ----- " + gold + bold + "Help Mods 1" + gray + bold + " ----- ");
					p.sendMessage(gray + "> " + gold + "/ban " + gray + "- ban a player" + red + it + " like a boss");
					p.sendMessage(gray + "> " + gold + "/tempban " + gray + "- temporary ban a player");
					p.sendMessage(gray + "> " + gold + "/kick " + gray + "- kick a player");
					p.sendMessage(gray + "> " + gold + "/unban " + gray + "- pardon a player");
					p.sendMessage(gray + "> " + gold + "/frz " + gray + "- freze a player" + red + it + " Agent's favorite, Simon hates this!");
					p.sendMessage(gray + "> " + gold + "/whitelist " + gray + "- display whitelist commands");
					p.sendMessage(gray + "> " + gray + "Next page: /help mods 2");
					return true;
				}
	
				if (args[1].equalsIgnoreCase("2")) {
					p.sendMessage(gray + bold + " ----- " + gold + bold + "Help Mods 2" + gray + bold + " ----- ");
					p.sendMessage(gray + "> " + gold + "/v " + gray + "- vanish toggle");
					p.sendMessage(gray + "> " + gold + "/tp " + gray + "- teleport to a player or location");
					p.sendMessage(gray + "> " + gold + "/co i " + gray + "- enable the inspection wand");
					p.sendMessage(gray + "> " + gold + "/co l " + gray + "- perform a lookup");
					p.sendMessage(gray + "> " + gold + "/open " + gray + "- open a player's inventory");
					p.sendMessage(gray + "> " + gold + "/oe " + gray + "- open a player's Ender chest");
					p.sendMessage(gray + "> " + gray + "Next page: /help mods 3");
					return true;
				}

				if (args[1].equalsIgnoreCase("3")) {
					p.sendMessage(gray + bold + " ----- " + gold + bold + "Help Mods 2" + gray + bold + " ----- ");
					p.sendMessage(gray + "> " + gold + "/tps " + gray + "- check ticks per second");
					p.sendMessage(gray + "> " + gold + "/restart " + gray + "- restart the server");
					p.sendMessage(gray + "> " + gold + "/ultraban " + gray + "- super special command" + red + it + " in development! Don't use!");
					p.sendMessage(gray + "> " + gray + "More help: /help chatmods");
					return true;
				}

			}

			if (!(sender.hasPermission("arcane.mod") && sender.hasPermission("arcane.mod"))
					&& (args[0].equalsIgnoreCase("mods") || args[0].equalsIgnoreCase("chatmods"))) {
				p.sendMessage(gold + "[ArcaneSurvival] " + white + "You do not have permission to do that.");
				return true;
			}
			// if(args[0].equalsIgnoreCase("ignore")){
			// p.sendMessage(gray + bold + HR + re + gold + bold + " Help Ignore
			// " + gray + HR);
			// p.sendMessage(gray + "> " + gold + "/ignore <player> " + gray +
			// "- Ignore a player");
			// p.sendMessage(gray + "> " + gold + "/unignore <player> " + gray +
			// "- Unignore a player");
			// p.sendMessage(gray + "> " + gold + "/ignorelist " + gray + "-
			// View your ignore list");
			// }

		

			// p.sendMessage(red + "|||||||||||||" + green + bold + " You've
			// Reached the " + yellow + bold + "SUPREME" + green + bold + " Help
			// Menu " + red + "|||||||||||||");
			// p.sendMessage(rad + ChatColor.RESET + " We're here to help!" +
			// rad);
			// p.sendMessage(ChatColor.DARK_PURPLE + "Want to go to the
			// SupremeSpawn?" + red + bold + " TYPE /SPAWN!");
			// p.sendMessage(ChatColor.DARK_PURPLE + "Want to go to your
			// supremeHome?" + red + bold + " TYPE /HOME! BOOM YOU'RE HOME!");
			// p.sendMessage(ChatColor.DARK_PURPLE + "Looking to enable " +
			// green + "Player Versus Player " + ChatColor.DARK_PURPLE +
			// "combat?" + red + bold + " TYPE /PVP!");
			// p.sendMessage(ChatColor.DARK_PURPLE + "Want see when a person has
			// last been online Supreme?" + red + bold + " TYPE /SEEN!");
			// p.sendMessage(ChatColor.DARK_PURPLE + "Chest protection!" +
			// yellow + it + " EZ-PZ-LMN-SQZY" + red + bold + " TYPE /HELP
			// LWC!");


		}
		
		return false;
	}
	
	public final class ArcaneEvents implements Listener {
		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent e) {
			Player player = e.getPlayer();
			if (!player.hasPlayedBefore()) {
				Location playerLoc = player.getLocation();
				//Makes sure all new players spawn in the same place
				playerLoc.setX(290.49D);
				playerLoc.setY(65.0D);
				playerLoc.setZ(50.64D);
				playerLoc.setPitch(0.0F);
				playerLoc.setYaw(0.0F);
	
				player.teleport(playerLoc);
	
				Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName()
						+ " has joined Arcane for the first time.");
			}
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent e) {
			if ((e.getBlock().getType() == Material.DIAMOND_ORE)
					&& (e.getBlock().getY() <= 20)) {
				Player player = e.getPlayer();
				Date date = new Date();

				/*
				 * 
				 * Bukkit.broadcast(ChatColor.GOLD + "[Alert] " + ChatColor.WHITE +
				 * player.getName() + ChatColor.GOLD + " mined diamond ore.",
				 * "bukkit.broadcast.admin");
				 */

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
