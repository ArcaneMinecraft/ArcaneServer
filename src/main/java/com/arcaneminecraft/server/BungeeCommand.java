package com.arcaneminecraft.server;

import com.arcaneminecraft.api.BungeeCommandUsage;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BungeeCommand extends Command {
    BungeeCommand(BungeeCommandUsage bcu) {
        super(bcu.getName(), bcu.getDescription(), bcu.getUsage(), Arrays.asList(bcu.getAliases()));
    }

    @Override
    public boolean execute(CommandSender sender, String label,String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GRAY + "This is an ArcaneBungee command. You must run it from BungeeCord proxy console.");
            return true;
        }

        if (label.contains(":")) {
            BaseComponent send = new TextComponent("This is an ArcaneBungee command. Try '/" + this.getName() + "' instead");
            send.setColor(ChatColor.GRAY);
            send.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + this.getName() + " "));
            ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, send);
        } else {
            sender.sendMessage(ChatColor.GRAY + "This is an ArcaneBungee command. Please connect through Arcane Survival to use this command.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return ImmutableList.of();
    }
}