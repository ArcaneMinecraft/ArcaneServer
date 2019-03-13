package com.arcaneminecraft.server.command;

import com.arcaneminecraft.api.ArcaneText;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class OpmeCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.spigot().sendMessage(ArcaneText.noConsoleMsg());
            return true;
        }
        Player p = (Player) sender;

        if (p.hasPermission("arcane.command.opme")) {
            p.setOp(true);
            p.spigot().sendMessage(ChatMessageType.SYSTEM, new TranslatableComponent("commands.op.success", p.getName()));
        } else {
            p.spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.noPermissionMsg());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
