package com.arcaneminecraft.survival;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.arcaneminecraft.ArcaneCommons;

final class ArcAFK implements CommandExecutor, Listener {
	private final ArcaneSurvival plugin;
	private final HashMap<Player,Integer> afkCounter = new HashMap<>();
	private static final int AFK_COUNTDOWN = 10; // 10 rounds (this * AFK_CHECK = 5 minute) countdown to being afk
	private static final long AFK_CHECK = 600L; // run every 600 ticks (30 seconds)
	private static final String FORMAT_AFK = ChatColor.GRAY + "* ";
	private static final String DISPLAY_TAG_AFK = "[AFK] ";
	private static final String PL_TAG_AFK = ChatColor.DARK_PURPLE + "[AFK] " + ChatColor.RESET;
	
	ArcAFK(ArcaneSurvival plugin) {
		this.plugin = plugin;
		// if players are already online
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			afkCounter.put(p, AFK_COUNTDOWN);
		}
		
		// resetTimer Queue Runner (because creating multiple new object didn't seem ideal at all)
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Iterator<HashMap.Entry<Player,Integer>> i = afkCounter.entrySet().iterator(); i.hasNext(); ) {
					HashMap.Entry<Player, Integer> e = i.next();
					
					if (e.getValue() == 0) {
						// Count went down to zero
						setAFK(e.getKey());
						i.remove();
					} else {
						e.setValue(e.getValue() - 1);
					}
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0L, AFK_CHECK); // run every 1200 ticks (1 minute)
		
	}
	
	void onDisable() {
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			unsetAFK(p);
		}
	}
	
	/**
	 * Shows whether the player is afk.
	 * @param p Player in question.
	 * @return ture if the player is afk.
	 */
	public boolean isAFK(Player p) {
		return !afkCounter.containsKey(p);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ArcaneCommons.noConsoleMsg());
			return true;
		}
		Player p = (Player)sender;
		if (!isAFK(p)) setAFK(p);
		afkCounter.remove(p);
		
		return true;
	}
	
	private void setAFK(Player p) {
		// For iterator safety, the `afkCounter.remove()` is not called in here.
		if (isAFK(p)) return;
		
		// Player is now afk.
		p.setSleepingIgnored(true);
		if (!p.getDisplayName().startsWith(DISPLAY_TAG_AFK)) p.setDisplayName(DISPLAY_TAG_AFK + p.getDisplayName());
		p.setPlayerListName(PL_TAG_AFK + p.getPlayerListName());
		p.sendMessage(FORMAT_AFK + "You are now AFK.");
	}
	
	private void unsetAFK(Player p) {
		// If previous value was not null (if player was not afk) 
		if (afkCounter.put(p, AFK_COUNTDOWN) != null)
			return;
		// only truly afk players below this comment
		
		// Check tab list string
		String pdn = p.getDisplayName();
		String pln = p.getPlayerListName();
		p.setSleepingIgnored(false);
		if (pdn.startsWith(DISPLAY_TAG_AFK)) p.setDisplayName(pdn.substring(6));
		p.setPlayerListName(pln.substring(8)); // this thing seems to do some advanced computation ;-;
		p.sendMessage(FORMAT_AFK + "You are no longer AFK.");
	}
	
	@EventHandler (priority=EventPriority.MONITOR)
	public void detectJoin (PlayerJoinEvent e) { 
		afkCounter.put(e.getPlayer(), AFK_COUNTDOWN);
	}
	
	@EventHandler (priority=EventPriority.MONITOR)
	public void detectQuit (PlayerQuitEvent e) {
		afkCounter.remove(e.getPlayer());
	}
	
	// TODO Running command does not reset AFK countdown
	@EventHandler (priority=EventPriority.MONITOR)
	public void detectCommand (PlayerCommandPreprocessEvent e) { unsetAFK(e.getPlayer()); }
	
	@EventHandler (priority=EventPriority.MONITOR)
	public void detectChat (AsyncPlayerChatEvent e) { unsetAFK(e.getPlayer()); }
	
	@EventHandler (priority=EventPriority.MONITOR)
	public void detectMotion (PlayerMoveEvent e) { unsetAFK(e.getPlayer()); }
}
