package com.arcaneminecraft.server.command;

import com.arcaneminecraft.api.ArcaneColor;
import com.arcaneminecraft.api.ArcaneText;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class DiamondStoneRatioCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player)
                ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.usage(cmd.getUsage()));
            else
                sender.spigot().sendMessage(ArcaneText.usage(cmd.getUsage()));
            return true;
        }

        Player p = Bukkit.getPlayer(args[0]);

        if (p == null) {
            if (sender instanceof Player)
                ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.playerNotFound());
            else
                sender.spigot().sendMessage(ArcaneText.playerNotFound());
            return true;
        }

        int diasMine = p.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE);
        int diasPut = p.getStatistic(Statistic.USE_ITEM, Material.DIAMOND_ORE);
        int stonesMine = p.getStatistic(Statistic.MINE_BLOCK, Material.STONE)
                + p.getStatistic(Statistic.MINE_BLOCK, Material.ANDESITE)
                + p.getStatistic(Statistic.MINE_BLOCK, Material.DIORITE)
                + p.getStatistic(Statistic.MINE_BLOCK, Material.GRANITE);
        int stonesPut = p.getStatistic(Statistic.USE_ITEM, Material.STONE)
                + p.getStatistic(Statistic.USE_ITEM, Material.ANDESITE)
                + p.getStatistic(Statistic.USE_ITEM, Material.DIORITE)
                + p.getStatistic(Statistic.USE_ITEM, Material.GRANITE);
        int diasEffective = diasMine - diasPut;
        int stonesEffective = stonesMine - stonesPut;

        BaseComponent pl = ArcaneText.playerComponentSpigot(sender);
        pl.setColor(ArcaneColor.FOCUS);

        DecimalFormat d = new DecimalFormat();
        d.setMaximumFractionDigits(3);

        TextComponent mineRatio = new TextComponent(d.format(stonesMine == 0 ? "\u221e" : (float) diasMine/stonesMine));
        TextComponent putRatio = new TextComponent(d.format(stonesPut == 0 ? "\u221e" : (float) diasPut/stonesPut));
        TextComponent effectiveRatio = new TextComponent(d.format(stonesPut == 0 ? "\u221e" : (float) diasEffective/stonesEffective));

        mineRatio.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(diasMine + "/" + stonesMine).create()));
        putRatio.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(diasPut + "/" + stonesPut).create()));
        effectiveRatio.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(diasEffective + "/" + stonesEffective).create()));

        effectiveRatio.setColor(ArcaneColor.FOCUS);

        // TODO: Put this into translatables
        TranslatableComponent data = new TranslatableComponent(
                "%s (%s - %s)",
                effectiveRatio, mineRatio, putRatio
        );
        data.setColor(ArcaneColor.CONTENT);

        TranslatableComponent send = new TranslatableComponent(
                "D:S for %s: %s",
                pl, data
        );
        send.setColor(ArcaneColor.HEADING);

        if (sender instanceof Player)
            ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, send);
        else
            sender.spigot().sendMessage(send);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length <= 1) {
            /* Default Bukkit "null" is a list of online players
            Collection<? extends Player> online = Bukkit.getOnlinePlayers();

            List<String> l = new ArrayList<>();
            for(Player p : online) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                    l.add(p.getName());
            }

            if (!l.isEmpty())
                return l;
            */
            return null;
        }
        return Collections.emptyList();
    }
}
