package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.ColorPalette;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.HashSet;

final class LocalChat implements CommandExecutor, Listener {
    private final ArcaneServer plugin;
    private static final String CHAT_TAG = ChatColor.GREEN + "(local)";
    private final int maxRange;
    private final int defaultRange;
    private final HashSet<Player> toggled;
    private final HashSet<Player> global;
    private final HashMap<Player,Integer> range;

    LocalChat(ArcaneServer plugin) {
        this.plugin = plugin;
        this.maxRange = plugin.getConfig().getInt("local-chat.max-range", 500);
        this.defaultRange = plugin.getConfig().getInt("local-chat.default-range", 40);
        this.toggled = new HashSet<>();
        this.global = new HashSet<>();
        this.range = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("global")) {
            if (args.length == 0) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.usage(cmd.getUsage()));
                else
                    sender.spigot().sendMessage(ArcaneText.usage(cmd.getUsage()));

                return true;
            }

            String msg = String.join(" ", args);

            if (sender instanceof Player) {
                global.add((Player)sender);
                ((Player) sender).chat(msg);
                return true;
            }

            // Only server console or player can send message.
            if (!(sender instanceof ConsoleCommandSender))
                return true;

            TranslatableComponent chat = new TranslatableComponent("chat.type.text", ArcaneText.playerComponentSpigot(sender), msg);

            Player pms = null;
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (pms == null) pms = p;
                p.spigot().sendMessage(ChatMessageType.CHAT, chat);
            }

            if (pms == null) {
                sender.sendMessage("Nobody is online to receive or relay the message.");
                return true;
            }

            sender.spigot().sendMessage(chat);
            plugin.getPluginMessenger().chat("Server",null,null, msg, pms);
            return true;
        }

        // Player-only Zone
        if (!(sender instanceof Player)) {
            sender.spigot().sendMessage(ArcaneText.noConsoleMsg());
            return true;
        }

        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("local")) {
            if (args.length == 0) {
                p.spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.usage(cmd.getUsage()));
                return true;
            }

            broadcastLocal(p, String.join(" ", args));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("localtoggle")) {
            BaseComponent send = new TextComponent("Local chat toggle is ");
            send.setColor(ColorPalette.CONTENT);
            if (toggled.add(p)){
                BaseComponent on = new TranslatableComponent("options.on");
                on.setColor(ColorPalette.POSITIVE);
                send.addExtra(on);
            } else {
                toggled.remove(p);
                BaseComponent off = new TranslatableComponent("options.off");
                off.setColor(ColorPalette.NEGATIVE);
                send.addExtra(off);
            }

            p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("localrange")) {
            if (args.length == 0) {
                BaseComponent send = new TextComponent("Your local chat range is ");
                send.setColor(ColorPalette.CONTENT);

                BaseComponent range = new TextComponent(String.valueOf(getRadius(p)));
                range.setColor(ColorPalette.FOCUS);
                send.addExtra(range);

                p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
                return true;
            }

            int r;
            try {
                r = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                BaseComponent send = new TranslatableComponent("commands.generic.num.invalid", args[0]);
                send.setColor(ChatColor.RED);
                p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
                return true;
            }

            // Use null to validate range
            BaseComponent oob = ArcaneText.numberOutOfRange(r, 1, maxRange);
            if (oob != null) {
                p.spigot().sendMessage(oob);
                return true;
            }

            range.put(p, r);

            BaseComponent send = new TextComponent("Local chat range is set to ");
            send.setColor(ColorPalette.CONTENT);

            BaseComponent range = new TextComponent(String.valueOf(getRadius(p)));
            range.setColor(ColorPalette.FOCUS);
            send.addExtra(range);

            p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
            return true;
        }

        return false;
    }

    private int getRadius(Player p) {
        Integer r = range.get(p);
        return (r == null) ? defaultRange : r;
    }

    private void broadcastLocal (Player p, String msg) {
        int r = getRadius(p);
        // Async event
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            // 1. Get all the recipients
            HashSet<Player> recipients = new HashSet<>();

            for (Player recipient : plugin.getServer().getOnlinePlayers()) {
                if (recipient.getWorld().equals(p.getWorld())
                        && recipient.getLocation().distanceSquared(p.getLocation()) <= r * r)
                    recipients.add(recipient);
            }

            // 2. Create message
            TextComponent send = new TextComponent();
            send.setColor(ChatColor.GRAY);

            // Beginning: tag
            BaseComponent a = new TextComponent();

            BaseComponent b = new TextComponent(CHAT_TAG);

            a.addExtra(b);

            // name
            b = new TextComponent(" <" + p.getDisplayName() + "> ");
            b.setColor(ChatColor.WHITE);
            a.addExtra(b);

            // Add a click action only to the beginning
            a.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/l "));

            // Hover event to show list of players who received the message
            StringBuilder list = new StringBuilder();
            for (Player rp : recipients)
                list.append(", ").append(rp.getName());

            // Get rid of leading comma and space
            list.delete(0, 2);

            a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Recipient" + (recipients.size() == 1 ? "" : "s") + ": " + list).create()));

            send.addExtra(a);

            // Later: message
            a = ArcaneText.url(msg);
            a.setItalic(true);
            send.addExtra(a);

            // Send Messages
            for (Player rp : recipients)
                rp.spigot().sendMessage(send);
        });
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void chatToggle (AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        // If chatted with /global OR local is not toggled: let the chat through
        if (global.remove(p) || !toggled.contains(p)) {
            return;
        }

        e.setCancelled(true);
        broadcastLocal(p, e.getMessage());
    }
}