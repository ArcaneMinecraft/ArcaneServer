package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.ColorPalette;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class ArcaneServer extends JavaPlugin {
    private ArcAFK afk;
    private PluginMessenger pluginMessenger;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        pluginMessenger = new PluginMessenger(this);
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pluginMessenger);

        // X-Ray notification Logging
        getServer().getMessenger().registerOutgoingPluginChannel(this, "ArcaneAlert");
        getServer().getPluginManager().registerEvents(new Alert(this), this);


        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
        getServer().getPluginManager().registerEvents(new Greylist(), this);
        // this must come before AFK
        getServer().getPluginManager().registerEvents(new PlayerListRole(this), this);

        HelpCommand hc = new HelpCommand(this);
        getCommand("help").setExecutor(hc);
        getServer().getPluginManager().registerEvents(hc, this);

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
                if (sender instanceof Damageable)
                    ((Damageable) sender).setHealth(0);
                else
                    sender.sendMessage("You must be a damageable entity");
                return true;
            }
            // For selected kill to go through, players will need Minecraft's kill permission in the end.
            if (sender instanceof Player) ((Player) sender).performCommand("minecraft:kill " + String.join(" ", args));
            if (sender instanceof ConsoleCommandSender)
                getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:kill " + String.join(" ", args));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("uuid")) {
            if (!sender.hasPermission("arcane.command.uuid")) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.noPermissionMsg());
                else
                    sender.spigot().sendMessage(ArcaneText.noPermissionMsg());
                return true;
            }

            if (args.length == 0) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.usage(getCommand("uuid").getUsage()));
                else
                    sender.spigot().sendMessage(ArcaneText.usage(getCommand("uuid").getUsage()));
                return true;
            }

            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                // OfflinePlayer can hold the thread: run as async.

                @SuppressWarnings("deprecation")
                OfflinePlayer pl = getServer().getOfflinePlayer(args[0]);

                String uuid = pl.getUniqueId().toString();

                BaseComponent send = new TextComponent(pl.getName());
                send.addExtra("'s UUID: ");
                send.addExtra(uuid);

                send.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, uuid));

                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, send);
                else
                    sender.spigot().sendMessage(send);
            });

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
                p.spigot().sendMessage(ChatMessageType.SYSTEM, new TranslatableComponent("commands.op.success", p.getName()));
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if ((command.getName().equalsIgnoreCase("kill") && sender.hasPermission("minecraft.command.kill"))
                || command.getName().equalsIgnoreCase("uuid"))
            return null;

        return Collections.emptyList();
    }
}
