package com.arcaneminecraft.survival;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

final class ArcAFK implements Listener {
	private static ArcaneSurvival arc;
	
	private static final HashMap<UUID, Integer> afkState = new HashMap<>(); // counts down toward [AFK] every second
	
	ArcAFK(ArcaneSurvival arc) {
		ArcAFK.arc = arc;
	}
	
	private static final Logger getLogger() {
		return arc.getLogger();
	}
	
	private static final Server getServer() {
		return arc.getServer();
	}
	
	private static final int AFK_COUNTDOWN = 300; // 5 minute countdown to being afk
	
	private static final String FORMAT_AFK = "§7";
	private static final String TAG_AFK = "§5[AFK] §r§f";
	private static final String TAG_GLOBAL = "§g";
	
	private static final String LOCAL_G_SHORT = "g";
	private static final String LOCAL_GLOBAL = "global";
	
	private void _disableAFK(Player pl)
	{
		String temp = pl.getPlayerListName();
		if (temp.isEmpty() || temp == null || temp.length() < 8)
		{
			getLogger().info("ArcaneChatUtils: empty player name? " + temp);
			temp = "I Am Error";
		}
		pl.setPlayerListName(temp.substring(8)); // magic number much? TAG_AFK is odd.
		afkState.put(pl.getUniqueId(), AFK_COUNTDOWN);
		pl.sendRawMessage(FORMAT_AFK + "You are no longer AFK.");
	}
	
	@EventHandler
	public void detectCommand (PlayerCommandPreprocessEvent pcpe)
	{
		Player pl = pcpe.getPlayer();
		UUID pID = pl.getUniqueId();
		String msg = pcpe.getMessage();
		
		if (afkState.get(pID) == null) afkState.put(pID, AFK_COUNTDOWN);
		
		int prevState = afkState.get(pID);
		afkState.put(pID, AFK_COUNTDOWN);
		
		if (prevState == 0)
		{
			_disableAFK(pl);
		}
		
		// I don't think this is a good implementation. Command Overshadowing would be better.
		if (msg.startsWith("/kill"))
		{
			if (msg.trim().equalsIgnoreCase("/kill"))
			{
				getServer().dispatchCommand(
					getServer().getConsoleSender(),
					"minecraft:kill " + pl.getUniqueId()
				);
			}
			else
			{
				if (pl.hasPermission("acu.murder")) return;
				// otherwise
				((CommandSender)pl).sendMessage("Sorry, this kind of murder is highly discouraged.");
			}
			pcpe.setCancelled(true);
		}
		else if (msg.startsWith("/minecraft:kill"))
		{
			if (msg.trim().equalsIgnoreCase("/minecraft:kill"))
			{
				getServer().dispatchCommand(
					getServer().getConsoleSender(), "minecraft:kill" + pl.getUniqueId()
				);
			}
			else
			{
				if (pl.hasPermission("acu.murder")) return;
				((CommandSender)pl).sendMessage("Sorry, this kind of murder is highly discouraged.");
			}
			pcpe.setCancelled(true);
		}
		// This is a weird implementation.
		else if (msg.startsWith("/" + LOCAL_GLOBAL))
		{
			pl.chat(msg.replaceFirst("/" + LOCAL_GLOBAL+" ",TAG_GLOBAL));
			pcpe.setCancelled(true);
		}
		else if (msg.startsWith("/" + LOCAL_G_SHORT + " "))
		{
			pl.chat(msg.replaceFirst("/" + LOCAL_G_SHORT+" ",TAG_GLOBAL));
			pcpe.setCancelled(true);
		}
	}
	
	@EventHandler
	public void detectChat (AsyncPlayerChatEvent pce)
	{
		Player pl = pce.getPlayer();
		UUID pID = pl.getUniqueId();
		String msg = pce.getMessage();
		
		if (afkState.get(pID) == null) afkState.put(pID, AFK_COUNTDOWN);
		
		int prevState = afkState.get(pID);
		afkState.put(pID, AFK_COUNTDOWN);
		
		if (prevState == 0)
		{
			_disableAFK(pl);
		}
		
		// TODO: FIX
		// if the player's local chat is toggled on
		/*if ((ltogState.get(pID) != null) && (ltogState.get(pID) > 0))
		{
			if (!msg.startsWith(TAG_GLOBAL))
			{
				pce.setCancelled(true);
			
				String[] chat = { "-r", ltogState.get(pID) + "", msg };
				shoutFunction(chat, (CommandSender)pl);
			}
			else
			{
				pce.setMessage(msg.replace(TAG_GLOBAL,""));
			}
		}*/
	}
	
	@EventHandler
	public void detectMotion (PlayerMoveEvent pme)
	{
		Player pl = pme.getPlayer();
		UUID pID = pl.getUniqueId();
		
		if (afkState.get(pID) == null) afkState.put(pID, AFK_COUNTDOWN);
		
		int prevState = afkState.get(pID);
		afkState.put(pID, AFK_COUNTDOWN);
		
		if (prevState == 0)
		{
			_disableAFK(pl);
		}
	}
	
	@EventHandler
	public void detectDiscon (PlayerQuitEvent pqe)
	{
		Player pl = pqe.getPlayer();
		UUID pID = pl.getUniqueId();
		
		if (afkState.get(pID) == null) afkState.put(pID, AFK_COUNTDOWN);
		
		int prevState = afkState.get(pID);
		afkState.put(pID, AFK_COUNTDOWN);
		
		if (prevState == 0)
		{
			_disableAFK(pl);
		}
	}
}
