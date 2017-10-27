package com.arcaneminecraft.survival;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.FlowerPot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Redstone;

import com.arcaneminecraft.api.ArcaneCommons;
import com.arcaneminecraft.api.ColorPalette;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.exceptions.ObjectAlreadyHasException;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;


public class Greylist implements CommandExecutor, Listener {
	private final ArcaneSurvival plugin;
	private final HashSet<Player> newPlayers = new HashSet<>();
	private static final String NEW_PERMISSION = "arcane.new";
	private NewPlayerListener listener = new NewPlayerListener();
	
	Greylist(ArcaneSurvival plugin) {
		this.plugin = plugin;
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			addNewPlayer(p);
		}
	}
	
	// In ALL cases, it adds to list only if the player has "arcane.new" permission.
	private void addNewPlayer(Player p) {
		// Check if player has new player permission
		if (!p.hasPermission(NEW_PERMISSION))
			return;
		
		newPlayers.add(p);
		
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
		
		TextComponent msg = ArcaneCommons.tagTC("Notice");
		msg.addExtra("You do ");
		TextComponent not = new TextComponent("not");
		not.setColor(ColorPalette.NEGATIVE);
		msg.addExtra(not);
		msg.addExtra(" have build permissions!\n Type ");
		TextComponent apply = new TextComponent("/apply");
		apply.setColor(ColorPalette.POSITIVE);
		msg.addExtra(apply);
		msg.addExtra(" to apply via our application.");
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/apply"));
		p.spigot().sendMessage(msg);
		p.sendMessage(ColorPalette.CONTENT + " You can ask a staff member to approve your application.");
		p.sendMessage("");
	}
	
	private void removeNewPlayer(Player p) {
		if (newPlayers.remove(p) && newPlayers.isEmpty()) {
			// unhook Listener
			HandlerList.unregisterAll(listener);
		}
	}
	
	private void noPerm(Cancellable e, Player p) {
		if (!p.hasPermission(NEW_PERMISSION)) {
			removeNewPlayer(p);
			return;
		}
		
		e.setCancelled(true);
		//p.sendMessage("From: " + e.getClass().getName());
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorPalette.CONTENT + "You do " 
				+ ColorPalette.NEGATIVE + "not" 
				+ ColorPalette.CONTENT + " have build permissions! Apply for it via " 
				+ ColorPalette.POSITIVE + "/apply" 
				+ ColorPalette.CONTENT + "!"));
		
		// Counter
		/*TextComponent msg = ArcaneCommons.tagTC("Notice");
		
		msg.addExtra("You don't have permission to do that.\n Type ");
		TextComponent apply = new TextComponent("/apply");
		apply.setColor(ColorPalette.POSITIVE);
		msg.addExtra(apply);
		msg.addExtra(" to apply via our application.");
		
		p.spigot().sendMessage(msg);*/
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Moderators will get a different message
		if (sender.hasPermission("arcane.chatmod")) {
			if (args.length == 0) {
				sender.sendMessage(ArcaneCommons.tagMessage("Usage: /greylist <player...>"));
			} else {
				for (String pl : args) {
					try {
						LuckPermsApi api = LuckPerms.getApi();
						api.getUser(pl).setPermission(api.getNodeFactory().makeGroupNode(api.getGroup("trusted")).build());
						sender.sendMessage(ArcaneCommons.tagMessage("Player " + ColorPalette.FOCUS + pl + ColorPalette.CONTENT + " greylisted!"));
					} catch (IllegalStateException | NoClassDefFoundError e) {
						sender.sendMessage(ArcaneCommons.tagMessage("Is LuckPerms loaded on the server?"));
					} catch (ObjectAlreadyHasException e) {
						// TODO Auto-generated catch block
						sender.sendMessage(ArcaneCommons.tagMessage("Player " + ColorPalette.FOCUS + pl + ColorPalette.CONTENT + " was already greylisted!"));
					}
				}
			}
			return true;
		}
		
		// if normal player ran it with some parameters
		if (args.length != 0) {
			sender.sendMessage(ArcaneCommons.noPermissionMsg(label,String.join(" ", args)));
			return true;
		}
		
		if (sender.hasPermission("arcane.trusted"))
			sender.sendMessage(ArcaneCommons.tagMessage("You are " + ColorPalette.POSITIVE + "on" + ColorPalette.CONTENT + " the greylist!"));
		
		else {
			sender.sendMessage(ArcaneCommons.tagMessage("You are " + ColorPalette.NEGATIVE + "not" + ColorPalette.CONTENT + " on the greylist!"));
			sender.sendMessage(ArcaneCommons.tagMessage("Apply for greylist using the /apply command, then talk with a staff member to become greylisted."));
		}

		return true;
	}
	
	@EventHandler (priority=EventPriority.HIGHEST)
	public void PlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (p.hasPermission("arcane.chatmod")) {
			// When staff joins and there's a non-greylisted player online
			for (Player pl : newPlayers)
				p.sendMessage(ArcaneCommons.tagMessage(ColorPalette.FOCUS + pl.getName() + ColorPalette.CONTENT + " is not greylisted yet."));
			return;
		}
		
		if (p.hasPermission("arcane.new")) {
			// Notify staff about this non-greylisted person
			plugin.getServer().broadcast(ArcaneCommons.tagMessage(ColorPalette.FOCUS + p.getName() + ColorPalette.CONTENT + " is not greylisted yet."), "arcane.chatmod");
			// Send non-greylisted message
			addNewPlayer(e.getPlayer());
		}
		
		// First join message
		if (!p.hasPlayedBefore())
			plugin.getServer().broadcastMessage(ColorPalette.META + p.getName()
					+ " has joined Arcane for the first time");
	}
	
	@EventHandler (priority=EventPriority.MONITOR)
	public void PlayerQuit(PlayerQuitEvent e) {
		removeNewPlayer(e.getPlayer());
	}
	
	// The class with all the actions player cannot commit.
	public class NewPlayerListener implements Listener {
		// Player and Entity
		@EventHandler (priority=EventPriority.HIGHEST) public void armorStandManipulate(PlayerArmorStandManipulateEvent e) { noPerm(e, e.getPlayer()); }
		@EventHandler (priority=EventPriority.HIGHEST) public void bucket(PlayerBucketEmptyEvent e) { noPerm(e, e.getPlayer()); }
		@EventHandler (priority=EventPriority.HIGHEST) public void bucket(PlayerBucketFillEvent e) { noPerm(e, e.getPlayer()); }
		@EventHandler (priority=EventPriority.HIGHEST) public void shearEntity(PlayerShearEntityEvent e) { noPerm(e, e.getPlayer()); }
		@EventHandler (priority=EventPriority.HIGHEST) public void unleashEntity(PlayerUnleashEntityEvent e) { noPerm(e, e.getPlayer()); }
		
		// Advanced Player and Entity
		@EventHandler (priority=EventPriority.HIGHEST) public void interactEntity(PlayerInteractEntityEvent e) {
			Player p = e.getPlayer();
			if (!p.hasPermission(NEW_PERMISSION))
				return;
			
			if (e.getRightClicked() instanceof Hanging) // Item Frame and paintings 
				noPerm(e, e.getPlayer());
		}
		
		@EventHandler (priority=EventPriority.HIGHEST) public void hangingBreakByEntity(HangingBreakByEntityEvent e) {
			Player p;
			if (!(e.getRemover() instanceof Player && (p = (Player)e.getRemover()).hasPermission(NEW_PERMISSION)))
				return;
			
			noPerm(e, p);
		}
		
		@EventHandler (priority=EventPriority.HIGHEST) public void damageByEntity(EntityDamageByEntityEvent e) {
			Player p;
			if (!(e.getDamager() instanceof Player && (p = (Player)e.getDamager()).hasPermission(NEW_PERMISSION)))
				return;
			
			Entity d = e.getEntity();
			
			// They can always hit Monsters + Slimes
			if (d instanceof Monster || d instanceof Slime)
				return;
			
			// They cannot hit important items
			if (
					d instanceof InventoryHolder || // Horses, other players
					d instanceof Hanging || // Item Frame and paintings
					d instanceof ArmorStand // Armor Stands
					)
				noPerm(e, p);
		}
		
		// Block
		@EventHandler (priority=EventPriority.HIGHEST) public void blockBreak(BlockBreakEvent e) { noPerm(e, e.getPlayer()); }
		@EventHandler (priority=EventPriority.HIGHEST) public void blockPlace(BlockPlaceEvent e) { noPerm(e, e.getPlayer()); }
		
		// Advanced Block
		@EventHandler (priority=EventPriority.HIGHEST) public void interact(PlayerInteractEvent e) {
			Player p = e.getPlayer();
			if (!p.hasPermission(NEW_PERMISSION))
				return;
			
			Action a = e.getAction();
			Block b = e.getClickedBlock();
			
			if (b == null)
				return;
			
			BlockState s = b.getState();
			Material m = e.getMaterial();
			
			if (
					a == Action.PHYSICAL || // No Automatic Redstone Triggers
					(a == Action.RIGHT_CLICK_BLOCK && (
							s.getData() instanceof Redstone || // No Redstone device clicks
							s instanceof FlowerPot || // because Flower Pot
							(m != null && ( // Prevent placement of entities below
									m == Material.ITEM_FRAME ||
									m == Material.ARMOR_STAND ||
									m == Material.PAINTING
							))
					))
			) 
				noPerm(e, e.getPlayer());
		}
		
		// Inventory
		@EventHandler (priority=EventPriority.HIGHEST) public void inventoryClick(InventoryClickEvent e) {
			Player p; 
			if (!(e.getWhoClicked() instanceof Player && (p = (Player)e.getWhoClicked()).hasPermission(NEW_PERMISSION)))
				return;
			
			Inventory i = e.getInventory();
			InventoryType it = i.getType();
			
			// TODO: Allow crafting table, enchantment table, Ender Chest, and anything else that doesn't affect other players.
			if (
					it == InventoryType.PLAYER ||
					it == InventoryType.CRAFTING ||
					it == InventoryType.WORKBENCH ||
					it == InventoryType.ENDER_CHEST ||
					it == InventoryType.ENCHANTING ||
					it == InventoryType.MERCHANT)
				return;
			
			InventoryAction a = e.getAction();
			
			// If item ends up in different inventory, block it.
			// Caveat: If holding item on cursor, spamming it enough will make it pass through.
			if (
					e.getClickedInventory() == i ||
					a == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
					a == InventoryAction.COLLECT_TO_CURSOR) noPerm(e, p);
		}
		
	}
	
}
