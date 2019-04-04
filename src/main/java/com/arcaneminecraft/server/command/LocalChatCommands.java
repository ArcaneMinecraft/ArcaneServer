package com.arcaneminecraft.server.command;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.ArcaneColor;
import com.arcaneminecraft.server.ArcaneServer;
import com.arcaneminecraft.server.SpigotLocaleTool;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class LocalChatCommands implements TabExecutor, Listener {
    private final ArcaneServer plugin;
    private final int maxRange;
    private final int defaultRange;
    private final HashSet<Player> toggled;
    private final HashSet<Player> global;
    private final HashMap<Player,Integer> range;

    public LocalChatCommands(ArcaneServer plugin) {
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
            plugin.getPluginMessenger().chat("Server", null, null, msg, null, pms);
            return true;
        }

        // Player-only Zone
        if (!(sender instanceof Player)) {
            sender.spigot().sendMessage(ArcaneText.noConsoleMsg());
            return true;
        }

        Player p = (Player) sender;
        Locale locale = SpigotLocaleTool.parse(p.getLocale());

        if (cmd.getName().equalsIgnoreCase("local")) {
            if (args.length == 0) {
                p.spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.usage(cmd.getUsage()));
                return true;
            }

            broadcastLocal(p, args);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("localtoggle")) {
            BaseComponent tog;
            if (toggled.add(p)){
                tog = new TranslatableComponent("options.on");
                tog.setColor(ArcaneColor.POSITIVE);
            } else {
                toggled.remove(p);
                tog = new TranslatableComponent("options.off");
                tog.setColor(ArcaneColor.NEGATIVE);
            }
            BaseComponent send = ArcaneText.translatable(locale, "commands.localtoggle", tog);
            send.setColor(ArcaneColor.CONTENT);

            p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("localrange")) {
            if (args.length == 0) {
                BaseComponent send = ArcaneText.translatable(locale, "commands.localrange", getRadius(p));
                send.setColor(ArcaneColor.CONTENT);

                p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
                return true;
            }

            int r;
            try {
                r = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                BaseComponent send = new TranslatableComponent("parsing.int.invalid", args[0]);
                send.setColor(ArcaneColor.NEGATIVE);
                p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
                return true;
            }

            // Use null to validate range
            BaseComponent oob = ArcaneText.numberOutOfRange(r, 1, maxRange);
            if (oob != null) {
                p.spigot().sendMessage(ChatMessageType.SYSTEM, oob);
                return true;
            }

            range.put(p, r);

            BaseComponent send = ArcaneText.translatable(locale, "commands.localrange.success", getRadius(p));
            send.setColor(ArcaneColor.CONTENT);
            p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
            return true;
        }

        return false;
    }

    private int getRadius(Player p) {
        Integer r = range.get(p);
        return (r == null) ? defaultRange : r;
    }

    private void broadcastLocal(Player p, String msg) {
        broadcastLocal(p, msg.split(" "));
    }

    private void broadcastLocal(Player p, String[] msg) {
        int r = getRadius(p);

        // Async event
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            // 1. Get all the recipients
            ArrayList<Player> recipients = new ArrayList<>();


            for (Player recipient : plugin.getServer().getOnlinePlayers()) {
                if (p.canSee(recipient)
                        && recipient.getWorld().equals(p.getWorld())
                        && recipient.getLocation().distanceSquared(p.getLocation()) <= r * r)
                    recipients.add(recipient);
            }

            Locale locale = SpigotLocaleTool.parse(p.getLocale());

            // Error: No player to send message to
            if (recipients.size() == 1) {
                BaseComponent send = ArcaneText.translatable(locale, "commands.local.nobody", r);
                send.setColor(ArcaneColor.CONTENT);
                p.spigot().sendMessage(ChatMessageType.SYSTEM, send);
                return;
            }

            // 2. Create recipients
            // Hover event to show list of players who received the message
            Iterator<Player> i = recipients.iterator();
            StringBuilder list = new StringBuilder("Recipients: ").append(i.next().getDisplayName());
            while (i.hasNext())
                list.append(", ").append(i.next().getDisplayName());

            // 3. Prepare messages
            TextComponent tag = new TextComponent(ArcaneText.translatableString(locale, "commands.local.tag"));
            tag.setColor(ChatColor.GREEN);
            tag.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/l "));
            tag.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(list.toString())}));
            tag.addExtra(" ");

            BaseComponent player = ArcaneText.playerComponentSpigot(p, list.toString());

            BaseComponent msgB = ArcaneText.url(msg);
            msgB.setColor(ArcaneColor.CONTENT);
            msgB.setItalic(true);

            BaseComponent chat = new TranslatableComponent("chat.type.text", player, msgB);

            // Send Messages
            for (Player rp : recipients) {
                tag.setText(ArcaneText.translatableString(SpigotLocaleTool.parse(rp.getLocale()), "commands.local.tag"));
                rp.spigot().sendMessage(ChatMessageType.CHAT, tag, chat);
            }
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}