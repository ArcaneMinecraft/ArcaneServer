package com.arcaneminecraft.server.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Damageable;

import java.util.Collections;
import java.util.List;

public class KillCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Damageable) {
                ((Damageable) sender).setHealth(0);
            } else {
                sender.sendMessage("You must be a damageable entity");
            }
            return true;
        }

        // For selected kill to go through, players will need Minecraft's kill permission in the end.
        Bukkit.getServer().dispatchCommand(sender, "minecraft:kill " + String.join(" ", args));
        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
