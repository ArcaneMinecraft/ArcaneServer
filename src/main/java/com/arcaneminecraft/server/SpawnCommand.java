package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneColor;
import com.arcaneminecraft.api.ArcaneText;
import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class SpawnCommand implements TabExecutor, Listener {
    private final ArcaneServer plugin;
    private final boolean spawnEnabled;
    private Location spawnPoint;

    SpawnCommand(ArcaneServer plugin) {
        this.plugin = plugin;
        this.spawnEnabled = plugin.getConfig().getBoolean("spawn.command-enabled", false);
        updateSpawnPoint();
    }

    private void updateSpawnPoint() {
        for (World w : plugin.getServer().getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                spawnPoint = w.getSpawnLocation();
                if (plugin.getConfig().getBoolean("spawn.center-on-block", false))
                    spawnPoint.add(0.5,0.5,0.5);
                break;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setworldspawn")) {
            plugin.getServer().dispatchCommand(sender, "minecraft:setworldspawn" + ((args.length == 0) ? "" : " " + String.join(" ", args)));
            updateSpawnPoint();
            return true;
        }

        if (!spawnEnabled) {
            sender.sendMessage(ArcaneColor.CONTENT + "Spawn is disabled in this world");
            return true;
        }

        if (!(sender instanceof Entity)) {
            sender.spigot().sendMessage(ArcaneText.noConsoleMsg());
            return true;
        }

        if (sender instanceof Player && !sender.hasPermission("arcane.command.spawn")) {
            ((Player)sender).spigot().sendMessage(ArcaneText.noPermissionMsg());
            return true;
        }

        ((Entity)sender).teleport(spawnPoint, PlayerTeleportEvent.TeleportCause.COMMAND);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("setworldspawn")) {
            if (args.length <= 3 && args[args.length - 1].isEmpty())
                return ImmutableList.of("~");
        }

        return ImmutableList.of();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        if (!e.isBedSpawn())
            e.setRespawnLocation(spawnPoint);
    }
}
