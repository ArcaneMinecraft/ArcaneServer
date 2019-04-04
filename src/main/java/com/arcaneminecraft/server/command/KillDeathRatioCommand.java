package com.arcaneminecraft.server.command;

import com.arcaneminecraft.api.ArcaneColor;
import com.arcaneminecraft.api.ArcaneText;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;

public class KillDeathRatioCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p;
        if (args.length == 0) {
            if (sender instanceof Player) {
                p = (Player) sender;
            } else {
                sender.spigot().sendMessage(ArcaneText.usage("/killdeathratio <player>"));
                return true;
            }
        } else {
            p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.playerNotFound());
                else
                    sender.spigot().sendMessage(ArcaneText.playerNotFound());
                return true;
            }
        }

        int playerKills = p.getStatistic(Statistic.PLAYER_KILLS);
        int kills = p.getStatistic(Statistic.MOB_KILLS) + playerKills;
        int deaths = p.getStatistic(Statistic.DEATHS);
        int deathsByPlayer = p.getStatistic(Statistic.ENTITY_KILLED_BY, EntityType.PLAYER);

        DecimalFormat d = new DecimalFormat();
        d.setMaximumFractionDigits(3);

        TextComponent totalRatio = new TextComponent(kills + "/" + deaths + " (" + d.format(deaths == 0 ? "\u221e" : (float) kills / deaths) + ")");
        TextComponent playerRatio = new TextComponent(playerKills + "/" + deathsByPlayer + " (" + d.format(deathsByPlayer == 0 ? "\u221e" : (float) playerKills / deathsByPlayer) + ")");
        totalRatio.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Total KD").create()));
        playerRatio.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Player KD").create()));
        totalRatio.setColor(ArcaneColor.FOCUS);
        playerRatio.setColor(ArcaneColor.FOCUS);

        BaseComponent pl = ArcaneText.playerComponentSpigot(p);

        TranslatableComponent send = new TranslatableComponent(
                "%s's KD Ratio: %s | %s",
                pl, totalRatio, playerRatio
        );
        send.setColor(ArcaneColor.CONTENT);

        if (sender instanceof Player)
            ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, send);
        else
            sender.spigot().sendMessage(send);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        return null;
    }
}
