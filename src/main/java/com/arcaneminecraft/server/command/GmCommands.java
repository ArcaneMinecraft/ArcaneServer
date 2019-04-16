package com.arcaneminecraft.server.command;

import com.arcaneminecraft.api.ArcaneText;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class GmCommands implements TabExecutor {
    private static final String GMS_OTHER_PERM = "arcane.command.gms.other";
    private static final String GMC_OTHER_PERM = "arcane.command.gmc.other";
    private static final String GMA_OTHER_PERM = "arcane.command.gma.other";
    private static final String GMSP_OTHER_PERM = "arcane.command.gmsp.other";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target;
        if (args.length != 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ArcaneText.playerNotFound());
                else
                    sender.spigot().sendMessage(ArcaneText.playerNotFound());
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("Usage: /gm<mode> <player>");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("gms")) {
            target.setGameMode(GameMode.SURVIVAL);
        } else if (cmd.getName().equalsIgnoreCase("gmc")) {
            target.setGameMode(GameMode.CREATIVE);
        } else if (cmd.getName().equalsIgnoreCase("gma")) {
            target.setGameMode(GameMode.ADVENTURE);
        } else if (cmd.getName().equalsIgnoreCase("gmsp")) {
            target.setGameMode(GameMode.SPECTATOR);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("gms") && sender.hasPermission(GMS_OTHER_PERM)
                || cmd.getName().equalsIgnoreCase("gmc") && sender.hasPermission(GMC_OTHER_PERM)
                || cmd.getName().equalsIgnoreCase("gma") && sender.hasPermission(GMA_OTHER_PERM)
                || cmd.getName().equalsIgnoreCase("gmsp") && sender.hasPermission(GMSP_OTHER_PERM)
        ) {
            return null;
        }
        return Collections.emptyList();
    }
}
