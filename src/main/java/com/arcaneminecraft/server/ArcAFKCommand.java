package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneColor;
import com.arcaneminecraft.api.ArcaneText;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

import java.util.*;

final class ArcAFKCommand implements TabExecutor, Listener {
    private final ArcaneServer plugin;
    private final HashMap<Player, Integer> afkCounter = new HashMap<>();
    private final HashMap<Player, BukkitRunnable> unsetAFKCondition = new HashMap<>();
    private final ArrayList<Player> afkOrder = new ArrayList<>();
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
        // For thread safety with repeating event, the `afkCounter.remove()` is not called in here.
        if (isAFK(p)) return;

        // Player is now afk.
        plugin.getPluginMessenger().afk(p, true);
        afkOrder.add(p);
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
        afkOrder.remove(p);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFullServerAndAfkPlayer(PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            Collection<? extends Player> online = plugin.getServer().getOnlinePlayers();
            if (online.size() == plugin.getServer().getMaxPlayers()) {
                Iterator<Player> i = afkOrder.iterator();
                if (i.hasNext()) {
                    i.next().kickPlayer("Sorry, but you were AFK and the server needs more room for another player to join"); // TODO: Message
                    e.allow();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectJoin(PlayerJoinEvent e) {
        afkCounter.put(e.getPlayer(), rounds);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectChat(AsyncPlayerChatEvent e) {
        unsetAFK(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectSneak(PlayerToggleSneakEvent e) {
        unsetAFK(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        afkCounter.remove(p);
        BukkitRunnable task = unsetAFKCondition.remove(p);
        if (task != null)
            task.cancel();
        afkOrder.remove(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void detectAttack(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();

        // if hit by an arrow or other projectile
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Entity)
                damager = (Entity) shooter;
        }

        // e.g. if a player hit another player
        if (damager instanceof Player) {
            unsetAFK((Player) damager);
            // pvp on an afk player
            if (e.getEntity() instanceof Player && isAFK((Player) e.getEntity()))
                e.setCancelled(true);
        }

        // Damage aspect handled by detectDamage() and detectDeath()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectDamage(EntityDamageEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Player) || !isAFK((Player) e.getEntity()))
            return;

        Player p = (Player) e.getEntity();

        if (p.getHealth() - e.getDamage() <= 0) {
            p.kickPlayer("You are about to die while you are AFK"); // TODO: Message
            e.setCancelled(true);
            return;
        }

        BukkitRunnable task = unsetAFKCondition.remove(p);
        if (task != null)
            task.cancel();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                unsetAFKCondition.remove(p);
            }
        };
        task.runTaskLaterAsynchronously(plugin, 40L);

        unsetAFKCondition.put(p, task);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectMotion(PlayerMoveEvent e) {
        if (!isAFK(e.getPlayer()) || unsetAFKCondition.containsKey(e.getPlayer())
                || (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()))
            return;

        BukkitRunnable task = new BukkitRunnable() {
            private final Player p = e.getPlayer();
            private final Location in = e.getFrom();

            @Override
            public void run() {
                // Calculate only by X and Z
                if (in.getWorld() != p.getWorld()
                        || NumberConversions.square(in.getX() - p.getLocation().getX())
                            + NumberConversions.square(in.getZ() - p.getLocation().getZ()) >= 0.6)
                    unsetAFK(p);
                unsetAFKCondition.remove(p);
            }
        };
        task.runTaskLaterAsynchronously(plugin, 10L);

        unsetAFKCondition.put(e.getPlayer(), task);
    }
}
