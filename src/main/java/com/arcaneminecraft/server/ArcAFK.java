package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.ColorPalette;
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

final class ArcAFK implements TabExecutor, Listener {
    private final ArcaneServer plugin;
    private final HashMap<Player, Integer> afkCounter = new HashMap<>();
    private final int rounds;
    private final String tag;

    ArcAFK(ArcaneServer plugin) {
        this.plugin = plugin;
        this.rounds = plugin.getConfig().getInt("afk.rounds", 10);
        this.tag = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("afk.tag", "[AFK]")) + ChatColor.RESET + " ";

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

    private BaseComponent formatAFK(String subject, String msg) {
        BaseComponent ret = new TranslatableComponent("chat.type.emote", subject, msg);
        ret.setColor(ColorPalette.CONTENT);
        return ret;
    }

    private void setAFK(Player p) {
        // For thread safety, the `afkCounter.remove()` is not called in here.
        if (isAFK(p)) return;

        // Player is now afk.
        p.setSleepingIgnored(true);
        p.setPlayerListName(tag + p.getPlayerListName());
        p.spigot().sendMessage(ChatMessageType.SYSTEM, formatAFK("You", "are now AFK"));
    }

    private void unsetAFK(Player p) {
        // If previous value was not null (if player was not afk)
        if (afkCounter.put(p, rounds) != null)
            return;
        // only truly afk players below this comment

        // Check tab list string
        String pln = p.getPlayerListName();
        p.setSleepingIgnored(false);
        p.setPlayerListName(pln.substring(8)); // this thing seems to do some advanced computation ;-;
        p.spigot().sendMessage(ChatMessageType.SYSTEM, formatAFK("You", "are no longer AFK"));
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
