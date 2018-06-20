package com.arcaneminecraft.server;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelpCommand implements TabExecutor {
    private final ArcaneServer plugin;

    HelpCommand(ArcaneServer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            // Help menu
            int page = args.length == 0 ? 1 : Integer.parseInt(args[0]);

            if (page < 1) {
                BaseComponent ret = new TranslatableComponent("commands.generic.num.tooSmall", String.valueOf(page), "1");
                ret.setColor(ChatColor.RED);
                if (sender instanceof Player)
                    ((Player)sender).spigot().sendMessage(ChatMessageType.SYSTEM, ret);
                else
                    sender.spigot().sendMessage(ret);
                return true;
            }

            // Get commands
            List<String> cmdList = getAllCommands(sender);
            int totalPages = (cmdList.size() - 1)/7 + 1;

            // 7 entries per page
            if (page > totalPages) {
                BaseComponent ret = new TranslatableComponent("commands.generic.num.tooBig", String.valueOf(page), String.valueOf(totalPages));
                ret.setColor(ChatColor.RED);
                if (sender instanceof Player)
                    ((Player)sender).spigot().sendMessage(ChatMessageType.SYSTEM, ret);
                else
                    sender.spigot().sendMessage(ret);
                return true;
            }

            int pg = Math.min(cmdList.size(), page*7);


            // First line
            BaseComponent header = new TranslatableComponent("commands.help.header", String.valueOf(page), String.valueOf(totalPages));
            header.setColor(ChatColor.DARK_GREEN);
            if (sender instanceof Player)
                ((Player)sender).spigot().sendMessage(ChatMessageType.SYSTEM, header);
            else
                sender.spigot().sendMessage(header);

            // Help topics
            for (int i = (page - 1)*7; i < pg; i++) {
                if (sender instanceof Player)
                    ((Player)sender).spigot().sendMessage(ChatMessageType.SYSTEM, usage(cmdList.get(i)));
                else
                    sender.spigot().sendMessage(usage(cmdList.get(i)));
            }

            // Last Line
            if (page == 1) {
                BaseComponent footer = new TranslatableComponent("commands.help.footer");
                footer.setColor(ChatColor.GREEN);
                if (sender instanceof Player)
                    ((Player)sender).spigot().sendMessage(ChatMessageType.SYSTEM, footer);
                else
                    sender.spigot().sendMessage(footer);
            }


            return true;
        } catch (NumberFormatException e) {
            // Not a number: descriptive help
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }


    private List<String> getAllCommands(CommandSender sender) {
        List<String> ret = new ArrayList<>();
        Collection<HelpTopic> cmds = plugin.getServer().getHelpMap().getHelpTopics();

        for (HelpTopic ht : cmds) {
            String cmd = ht.getName();
            if (!cmd.startsWith("/"))
                continue;

            cmd = cmd.substring(1);

            if (!ht.canSee(sender) || plugin.commandsToHide(sender).contains(cmd))
                continue;
            ret.add(cmd);
        }

        return ret;
    }

    private BaseComponent usage(String cmd) {
        return new TextComponent(plugin.getServer()
                .getPluginCommand(cmd)
                .getUsage());
        // TODO: This is too simple
    }
}
