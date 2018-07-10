package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.ArcaneColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

final class ArcAFKCommand implements TabExecutor, Listener {
    private final ArcaneServer plugin;
    private final HashMap<Player, Integer> afkCounter = new HashMap<>();
    private final int rounds;
    private final String tag;
    private final boolean modifyTabList;

    ArcAFKCommand(ArcaneServer plugin) {
        this.plugin = plugin;
        this.rounds = plugin.getConfig().getInt("afk.rounds", 10);
        this.tag = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("afk.tag", "[AFK]")) + ChatColor.RESET + " ";
        this.modifyTabList = plugin.getConfig().getBoolean("modify-tablist", false);

        // if players are already online
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            afkCounter.put(p, rounds);
        }


        new BukkitRunnable() {
            @Override
            public void run() {
                for (Iterator<HashMap.Entry<Player, Integer>> i = afkCounter.entrySet().iterator(); i.hasNext(); ) {
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
        }.runTaskTimerAsynchronously(plugin, 0L, plugin.getConfig().getLong("afk.interval", 600L));

    }

    void onDisable() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            unsetAFK(p);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.spigot().sendMessage(ArcaneText.noConsoleMsg());
            return true;
        }
        Player p = (Player) sender;
        if (!isAFK(p)) setAFK(p);
        afkCounter.remove(p);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private boolean isAFK(Player p) {
        return !afkCounter.containsKey(p);
    }

    BaseComponent formatAFK(Object subject, String msg) {
        BaseComponent ret = new TranslatableComponent("chat.type.emote", subject, msg);
        ret.setColor(ArcaneColor.CONTENT);
        return ret;
    }

    private void setAFK(Player p) {
        // For thread safety, the `afkCounter.remove()` is not called in here.
        if (isAFK(p)) return;

        // Player is now afk.
        plugin.getPluginMessenger().afk(p, true);
        p.setSleepingIgnored(true);
        if (modifyTabList)
            p.setPlayerListName(tag + p.getPlayerListName());
        p.spigot().sendMessage(ChatMessageType.SYSTEM, formatAFK("You", "are now AFK"));
        BaseComponent send = formatAFK(ArcaneText.playerComponentSpigot(p), "is now AFK");
        send.setColor(ArcaneColor.CONTENT);
        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl == p) continue;
            pl.spigot().sendMessage(ChatMessageType.SYSTEM, send);
        }
    }

    private void unsetAFK(Player p) {
        // If previous value was not null (if player was not afk)
        if (afkCounter.put(p, rounds) != null)
            return;

        // Player was afk
        if (plugin.isEnabled()) plugin.getPluginMessenger().afk(p, false);
        p.setSleepingIgnored(false);
        if (modifyTabList)
            p.setPlayerListName(p.getPlayerListName().substring(tag.length())); // this thing seems to do some advanced computation ;-;
        p.spigot().sendMessage(ChatMessageType.SYSTEM, formatAFK("You", "are no longer AFK"));
        BaseComponent send = formatAFK(ArcaneText.playerComponentSpigot(p), "is no longer AFK");
        send.setColor(ArcaneColor.CONTENT);
        for (Player pl : plugin.getServer().getOnlinePlayers()) {
            if (pl == p) continue;
            pl.spigot().sendMessage(ChatMessageType.SYSTEM, send);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectJoin(PlayerJoinEvent e) {
        afkCounter.put(e.getPlayer(), rounds);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectQuit(PlayerQuitEvent e) {
        afkCounter.remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectChat(AsyncPlayerChatEvent e) {
        unsetAFK(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectMotion(PlayerMoveEvent e) {
        unsetAFK(e.getPlayer());
    }
}
