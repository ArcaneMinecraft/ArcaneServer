package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.ColorPalette;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class ArcaneServer extends JavaPlugin {
    private ArcAFK afk;
    private PluginMessenger pluginMessenger;

    @Override
    public void onEnable() {
        pluginMessenger = new PluginMessenger(this);
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pluginMessenger);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
        getServer().getPluginManager().registerEvents(new Greylist(), this);
        // this must come before AFK
        getServer().getPluginManager().registerEvents(new PlayerListRole(this), this);

        getCommand("help").setExecutor(new HelpCommand(this));
        getCommand("afk").setExecutor(afk = new ArcAFK(this));
        getServer().getPluginManager().registerEvents(afk, this);
    }

    @Override
    public void onDisable() {
        afk.onDisable();
        for (Player p : getServer().getOnlinePlayers()) {
            p.setPlayerListName(p.getName());
        }
    }

    PluginMessenger getPluginMessenger() {
        return pluginMessenger;
    }

    // TODO: Implement TabCompleter (or TabExecutor)
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("kill")) {
            if (args.length == 0) {
                getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:kill" + (sender instanceof Player ? " " + ((Player) sender).getUniqueId() : ""));
                return true;
            }
            // For selected kill to go through, players will need Minecraft's kill permission in the end.
            if (sender instanceof Player) ((Player) sender).performCommand("minecraft:kill " + args[0]);
            if (sender instanceof ConsoleCommandSender)
                getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:kill " + args[0]);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("opme")) {
            if (!(sender instanceof Player)) {
                sender.spigot().sendMessage(ArcaneText.noConsoleMsg());
                return true;
            }
            Player p = (Player) sender;

            if (p.hasPermission("arcane.command.opme")) {
                p.setOp(true);
                // TODO: better message
                p.sendMessage("You have been opped.");
            } else {
                p.spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.noPermissionMsg());
            }
            return true;
        }

        // Useful username command
        // "very useful i give a perfect 5/7" -Simon, 2016
        // "this is too good to remove" -Simon, 2018
        if (cmd.getName().equalsIgnoreCase("username")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You'll always be named Server.");
                return true;
            }

            String name = ((Player) sender).getDisplayName();

            Random randy = new Random();

            String[] list = {
                    "It looks like your username is " + name + ".",
                    "Your username is " + name + ".",
                    "Your username is not Agentred100.",
                    "Username: " + name + ".",
                    ColorPalette.NEGATIVE + "[Username] " + ColorPalette.CONTENT + name + ".",
                    ColorPalette.HEADING + "[Username]" + ColorPalette.CONTENT + " At the moment, your username is " + name + ".",
                    ColorPalette.HEADING + "YOUR USERNAME IS " + ColorPalette.NEGATIVE + name + ".",
                    name
            };

            String r = list[randy.nextInt(list.length)];

            sender.sendMessage(ColorPalette.CONTENT + r);
            return true;
        }
        return false;
    }

    Set<String> commandsToHide(CommandSender sender) {
        // TODO: Implement this
        return Collections.emptySet();
    }
}
