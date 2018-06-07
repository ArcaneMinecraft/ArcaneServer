package com.arcaneminecraft.survival;

import com.arcaneminecraft.api.ColorPalette;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class ArcaneSurvival extends JavaPlugin {
    private ArcAFK afk;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(afk, this);

        getServer().getPluginManager().registerEvents(new Greylist(), this);

        // This must come before ArcAFK
        getServer().getPluginManager().registerEvents(new PlayerListRole(this), this);

        this.afk = new ArcAFK(this);
        getCommand("afk").setExecutor(afk);
        getServer().getPluginManager().registerEvents(afk, this);

    }

    @Override
    public void onDisable() {
        afk.onDisable();
        for (Player p : getServer().getOnlinePlayers()) {
            p.setPlayerListName(p.getName());
        }
        saveConfig();
    }

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
}
