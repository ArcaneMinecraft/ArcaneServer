package com.arcaneminecraft.server.listener;

import com.arcaneminecraft.server.ArcaneServer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class SleepDayListener implements Listener {
    private ArcaneServer plugin;
    private Set<Player> sleeping = new LinkedHashSet<>();
    private Set<Player> total = new HashSet<>();
    private BukkitTask schedule = null;

    public SleepDayListener(ArcaneServer plugin) {
        this.plugin = plugin;
        for (Player p : Bukkit.getOnlinePlayers()) {
            addPlayer(p);
        }
    }

    private void calculate() {
        // Let vanilla handle it if only one player online
        if (total.size() == 1)
            return;

        // Validate sleeping players (just in case)
        for (Player p : sleeping)
            if (!p.isSleeping())
                sleeping.remove(p);

        // If more than or equal to half the population sleeps
        if (sleeping.size() << 1 >= total.size()) {
            this.schedule = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ArrayList<Player> tempIgnored = new ArrayList<>();

                for (Player p : total) {
                    if (p.isSleeping()) {
                        p.setSleepingIgnored(true);
                        tempIgnored.add(p);
                    } else {
                        // TODO: Translatable
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new ComponentBuilder("Night skipped by vote (")
                                .append(String.valueOf(sleeping.size()))
                                .append(" of ")
                                .append(String.valueOf(total.size()))
                                .append(" slept)")
                                .create()
                        );
                    }
                }

                Bukkit.getScheduler().runTaskLater(plugin, task -> {
                    for (Player p : tempIgnored) {
                        if (!plugin.isAFK(p))
                            p.setSleepingIgnored(false);
                    }
                    sleeping.clear();
                }, 20L);
            }, 100L);
        }
    }

    public void addPlayer(Player p) {
        if (!p.isSleepingIgnored() && p.getWorld().getEnvironment() == World.Environment.NORMAL) {
            total.add(p);
            if (p.isSleeping())
                sleeping.add(p);
        }
        if (schedule != null && !schedule.isCancelled()) {
            schedule.cancel();
            calculate();
        }
    }

    public void removePlayer(Player p) {
        total.remove(p);
        sleeping.remove(p);
        calculate();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBedEnter(PlayerBedEnterEvent e) {
        if (e.isCancelled() || e.getPlayer().isSleepingIgnored())
            return;

        sleeping.add(e.getPlayer());
        calculate();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBedLeave(PlayerBedLeaveEvent e) {
        sleeping.remove(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        addPlayer(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDimensionChange(PlayerPortalEvent e) {
        if (e.isCancelled())
            return;

        if (!e.getPlayer().isSleepingIgnored()) {
            if (e.getFrom().getWorld().getEnvironment() == World.Environment.NORMAL
                    && e.getTo().getWorld().getEnvironment() != World.Environment.NORMAL) {
                removePlayer(e.getPlayer());
            }

            else if (e.getFrom().getWorld().getEnvironment() != World.Environment.NORMAL
                    && e.getTo().getWorld().getEnvironment() == World.Environment.NORMAL) {
                addPlayer(e.getPlayer());
            }
        }
    }
}
