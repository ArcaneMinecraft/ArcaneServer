package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.BungeeCommandUsage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PlayerEvents implements Listener {
    private final ArcaneServer plugin;

    PlayerEvents(ArcaneServer plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void commandEvent(TabCompleteEvent e) {
        String cmd = e.getBuffer().toLowerCase();

        if (!cmd.startsWith("/") || cmd.contains(" "))
            return; // TODO: Maybe return online players on the BungeeCord Network?

        CommandSender p = e.getSender();
        List<String> list = e.getCompletions();

        // TODO: Look into having our own command recommendations using (e.setCompletions()) and config file
        Iterator<String> iter = list.iterator();

        if (p.hasPermission("arcane.tabcomplete.hidecolon"))
            list.removeIf((String s) -> s.contains(":"));

        for (BungeeCommandUsage c : BungeeCommandUsage.values()) {
            String cmdCandidate = c.getCommand();
            if ((c.getPermission() == null || p.hasPermission(c.getPermission())) && cmdCandidate.toLowerCase().startsWith(cmd))
                if (!list.contains(cmdCandidate))
                    list.add(cmdCandidate);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chatEvent(AsyncPlayerChatEvent e) {
        HashSet<Player> recipients = new HashSet<>(e.getRecipients());
        e.getRecipients().clear(); // Destroy default event.

        // TODO: Prefix stuff (from LuckPerms)
        TranslatableComponent chat = new TranslatableComponent("chat.type.text", ArcaneText.playerComponentSpigot(e.getPlayer()), e.getMessage());

        for (Player p : recipients)
            p.spigot().sendMessage(ChatMessageType.CHAT, chat);

        plugin.getPluginMessenger().chat(e.getPlayer(), e.getMessage());
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void joinEvent(PlayerJoinEvent e) {
        e.setJoinMessage("");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void leaveEvent(PlayerQuitEvent e) {
        e.setQuitMessage("");
    }
}
