package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneColor;
import com.arcaneminecraft.api.ArcaneText;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
    private final HashSet<Player> kicked = new HashSet<>();
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

        BukkitRunnable task = new JustSetCondition(p);
        task.runTaskLaterAsynchronously(plugin, 200L);
        unsetAFKCondition.put(p, task);

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

        BukkitRunnable task = unsetAFKCondition.get(p);
        if (task != null)
            task.cancel();

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
                for (Player p : afkOrder) {
                    if (p.hasPermission("arcane.afk.stayonfullserver"))
                        continue;
                    p.kickPlayer("Sorry, but the server needs room for another player to join and you were AFK"); // TODO: Maybe move it to the BungeeCord plugin?
                    kicked.add(p);

                    BaseComponent send = null;
                    for (Player alert : plugin.getServer().getOnlinePlayers()) {
                        if (alert.hasPermission("arcane.afk.getkickmessage")) {
                            if (send == null) {
                                send = new TextComponent("Kicked AFK player ");
                                send.addExtra(ArcaneText.playerComponentSpigot(p));
                                send.addExtra(" to make room for ");
                                send.addExtra(ArcaneText.playerComponentSpigot(e.getPlayer()));
                                send.setColor(ArcaneColor.CONTENT);
                            }
                            alert.spigot().sendMessage(send);
                        }
                    }

                    e.allow();
                    return;
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
        // this may be called twice if kicked.remove(p) is called right away
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> kicked.remove(p), 10L);
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

        // Damage aspect handled by detectDamage()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectDamage(EntityDamageEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Player) || !isAFK((Player) e.getEntity()))
            return;

        Player p = (Player) e.getEntity();

        if (p.isOnline() && !(unsetAFKCondition.get(p) instanceof JustSetCondition) && p.getHealth() - e.getFinalDamage() <= 0) {
            e.setCancelled(true);
            if (!kicked.add(p))
                return;

            p.kickPlayer("You were about to die by " + e.getCause().name().toLowerCase() + " while you were AFK"); // TODO: Message

            BaseComponent send = null;
            for (Player alert : plugin.getServer().getOnlinePlayers()) {
                if (alert.hasPermission("arcane.afk.getkickmessage")) {
                    if (send == null) {
                        send = new TextComponent("Kicked AFK player ");
                        send.addExtra(ArcaneText.playerComponentSpigot(p));
                        send.addExtra(" - imminent death from '" + e.getCause().name().toLowerCase() + "'");
                        send.setColor(ArcaneColor.CONTENT);
                        Location l = p.getLocation();
                        send.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ()));
                    }
                    alert.spigot().sendMessage(send);
                }
            }
            return;
        }

        BukkitRunnable task = unsetAFKCondition.remove(p);
        if (task != null)
            task.cancel();

        task = new DamagedCondition(p);
        task.runTaskLaterAsynchronously(plugin, 40L);

        unsetAFKCondition.put(p, task);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void detectMotion(PlayerMoveEvent e) {
        BukkitRunnable task = unsetAFKCondition.get(e.getPlayer());

        // If player is not afk, reset their timer
        if (!isAFK(e.getPlayer())) {
            unsetAFK(e.getPlayer());
            return;
        }

        if (task instanceof MotionCondition || task instanceof DamagedCondition
                || (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()))
            return;

        if (task != null)
            task.cancel();

        task = new MotionCondition(e.getPlayer(), e.getFrom());
        task.runTaskLaterAsynchronously(plugin, 10L);

        unsetAFKCondition.put(e.getPlayer(), task);
    }

    private class JustSetCondition extends BukkitRunnable {
        private final Player p;
        private JustSetCondition(Player p) {
            this.p = p;
        }

        @Override
        public void run() {
            unsetAFKCondition.remove(p);
        }
    }

    private class DamagedCondition extends BukkitRunnable {
        private final Player p;

        private DamagedCondition(Player p) {
            this.p = p;
        }

        @Override
        public void run() {
            unsetAFKCondition.remove(p);
        }
    }

    private class MotionCondition extends BukkitRunnable {
        private final Player p;
        private final Location in;

        private MotionCondition(Player p, Location in) {
            this.p = p;
            this.in = in;
        }

        @Override
        public void run() {
            // Calculate only by X and Z
            if (in.getWorld() != p.getWorld()
                    || NumberConversions.square(in.getX() - p.getLocation().getX())
                    + NumberConversions.square(in.getZ() - p.getLocation().getZ()) >= 0.6)
                unsetAFK(p);
            unsetAFKCondition.remove(p);
        }
    }
}
