package com.arcaneminecraft.server.command;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.server.ArcaneServer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class UuidCommand implements TabExecutor {
    private final ArcaneServer plugin = ArcaneServer.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player)
                ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.usage(cmd.getUsage()));
            else
                sender.spigot().sendMessage(ArcaneText.usage(cmd.getUsage()));
            return true;
        }

        // OfflinePlayer can hold the thread: run as async.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            BaseComponent playerPart = ArcaneText.playerComponentSpigot(target);
            String uuid = target.getUniqueId().toString();

            Locale locale;
            if (sender instanceof Player) {
                String[] l = ((Player) sender).getLocale().split("_");
                locale = new Locale(l[0],l[1]);
            } else {
                locale = null;
            }

            BaseComponent send = ArcaneText.translatable(
                    locale,
                    "commands.uuid",
                    playerPart,
                    uuid
            );

            send.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, uuid));

            if (sender instanceof Player)
                ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, send);
            else
                sender.spigot().sendMessage(send);
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
