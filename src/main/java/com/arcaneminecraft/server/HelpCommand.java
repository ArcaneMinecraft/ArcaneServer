package com.arcaneminecraft.server;

import com.arcaneminecraft.api.BungeeCommandUsage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.*;

public class HelpCommand implements TabExecutor, Listener {
    private final ArcaneServer plugin;
    private List<CommandWrapper> commands;

    HelpCommand(ArcaneServer plugin) {
        this.plugin = plugin;
        loadCommands();
    }

    private void loadCommands() {
        commands = new ArrayList<>();
        Collection<String> temp = new HashSet<>();

        try {

            ConfigurationSection cf = plugin.getConfig().getConfigurationSection("help_commands");
            Set<String> confEntry = cf.getKeys(false);

            // Next thing you know Spigot 1.13 breaks this
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);

            SimpleCommandMap commMap = (SimpleCommandMap) f.get(plugin.getServer().getPluginManager());

            // First: BungeeCord Commands
            for (BungeeCommandUsage c : BungeeCommandUsage.values()) {
                commands.add(new CommandWrapper(c));
                temp.add(c.getName());
            }

            // Second: Registered commands
            for (Command c : commMap.getCommands()) {
                if (temp.contains(c.getName()))
                    continue;

                if (confEntry.contains(c.getName()))
                    commands.add(new CommandWrapper(c, cf.getConfigurationSection(c.getName())));
                else
                    commands.add(new CommandWrapper(c));

                temp.add(c.getName());
            }

            commands.sort(Comparator.comparing(CommandWrapper::getName));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().warning("Help menu will not work. Update plugin.");
            e.printStackTrace();
        }
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
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ret);
                else
                    sender.spigot().sendMessage(ret);
                return true;
            }

            // Get commands
            List<CommandWrapper> cmdList = getCommands(sender);
            int totalPages = (cmdList.size() - 1) / 7 + 1;

            // 7 entries per page
            if (page > totalPages) {
                BaseComponent ret = new TranslatableComponent("commands.generic.num.tooBig", String.valueOf(page), String.valueOf(totalPages));
                ret.setColor(ChatColor.RED);
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ret);
                else
                    sender.spigot().sendMessage(ret);
                return true;
            }

            int pg = Math.min(cmdList.size(), page * 7);


            // First line
            BaseComponent header = new TranslatableComponent("commands.help.header", String.valueOf(page), String.valueOf(totalPages));
            header.setColor(ChatColor.DARK_GREEN);
            if (sender instanceof Player)
                ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, header);
            else
                sender.spigot().sendMessage(header);

            // Help topics
            boolean showOrigin = sender.hasPermission("arcane.command.help.showorigin");
            for (int i = (page - 1) * 7; i < pg; i++) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, cmdList.get(i).getUsage(showOrigin));
                else
                    sender.spigot().sendMessage(cmdList.get(i).getUsage(showOrigin));
            }

            // Last Line
            if (page == 1) {
                BaseComponent footer = new TranslatableComponent("commands.help.footer");
                footer.setColor(ChatColor.GREEN);
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, footer);
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


    private List<CommandWrapper> getCommands(CommandSender sender) {
        List<CommandWrapper> ret = new ArrayList<>();

        for (CommandWrapper c : commands) {
            if (!c.hasPermission(sender))
                continue;
            ret.add(c);
        }

        return ret;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void pluginEnableEvent(PluginEnableEvent e) {
        loadCommands();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void pluginDisableEvent(PluginDisableEvent e) {
        loadCommands();
    }

    // TODO: Separate out this into 'commands' subpackage
    private final class CommandWrapper {
        private final String name;
        private final String usage;
        private final ClickEvent clickEvent;
        private final String permission;
        private final BaseComponent origin;

        private CommandWrapper(Command command) {
            this.name = command.getName();
            this.permission = command.getPermission();
            this.usage = command.getUsage().equals("") ? "/" + command.getName() : command.getUsage();
            this.origin = new TextComponent(command instanceof PluginCommand
                    ? ((PluginCommand) command).getPlugin().getName()
                    : command instanceof BukkitCommand
                    ? "Bukkit"
                    : "(Unknown)");
            this.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + name + " ");
            orininFormatting();
        }

        private CommandWrapper(Command command, ConfigurationSection configurationSection) {
            this.name = command.getName();
            this.permission = configurationSection.getString("permission", command.getPermission());
            this.usage = configurationSection.getString("usage",
                    command.getUsage().equals("") ? "/" + command.getName() : command.getUsage());
            this.origin =  new TextComponent((command instanceof PluginCommand
                    ? ((PluginCommand) command).getPlugin().getName()
                    : command instanceof BukkitCommand
                    ? "Bukkit"
                    : "(Unknown)" )+ " modified");
            this.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + name + " ");
            orininFormatting();
        }

        private CommandWrapper(BungeeCommandUsage command) {
            this.name = command.getName();
            this.permission = command.getPermission();
            this.usage = command.getUsage();
            this.origin = new TextComponent("(BungeeCord)");
            this.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + name + " ");
            orininFormatting();
        }

        private void orininFormatting() {
            this.origin.setColor(ChatColor.GRAY);
            this.origin.setItalic(true);
        }

        private String getName() {
            return name;
        }

        private BaseComponent getUsage(boolean showOrigin) {
            BaseComponent ret = usage.startsWith("commands.") ? new TranslatableComponent(usage) : new TextComponent(usage);
            if (showOrigin) {
                ret.addExtra(" ");
                ret.addExtra(origin);
            }
            ret.setClickEvent(clickEvent);
            return ret;
        }

        private boolean hasPermission(CommandSender sender) {
            return permission == null || sender.hasPermission(permission);
        }
    }
}
