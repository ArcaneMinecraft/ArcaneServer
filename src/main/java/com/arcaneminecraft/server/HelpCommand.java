package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.BungeeCommandUsage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.*;

public class HelpCommand implements TabExecutor, Listener {
    private final ArcaneServer plugin;
    private List<CommandWrapper> commandList;
    private Map<String, CommandWrapper> commandMap;
    private final BaseComponent notFoundMsg;
    private boolean reload = true;


    HelpCommand(ArcaneServer plugin) {
        this.plugin = plugin;
        this.notFoundMsg = new TranslatableComponent("commands.generic.notFound");
        this.notFoundMsg.setColor(ChatColor.RED);
    }

    private void loadCommands() {
        if (!reload)
            return;

        commandList = new ArrayList<>();
        commandMap = new HashMap<>();

        try {

            ConfigurationSection cf = plugin.getConfig().getConfigurationSection("help-override.commands");
            if (cf == null) {
                plugin.getLogger().warning("'help-override.commands' does not exist in config.yml. /help menu will not work.");
                return;
            }

            // Next thing you know Spigot 1.13 breaks this
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);

            SimpleCommandMap commMap = (SimpleCommandMap) f.get(plugin.getServer().getPluginManager());

            // First: BungeeCord Commands
            for (BungeeCommandUsage c : BungeeCommandUsage.values()) {
                if (cf.getBoolean(c.getName() + ".is-alias"))
                    continue;

                CommandWrapper cw = new CommandWrapper(c, cf.getConfigurationSection(c.getName()));
                commandList.add(cw);
                commandMap.put(c.getName(), cw);
                for (String ca : cw.getAliases())
                    commandMap.put(ca, cw);
            }

            // Second: Registered commands
            for (Command c : commMap.getCommands()) {
                if (commandMap.containsKey(c.getName()) || cf.getBoolean(c.getName() + ".is-alias"))
                    continue;

                CommandWrapper cw = new CommandWrapper(c, cf.getConfigurationSection(c.getName()));
                commandList.add(cw);
                commandMap.put(c.getName(), cw);

                for (String ca : cw.getAliases())
                    commandMap.put(ca, cw);
            }

            // Third: Remaining config.yml Commands
            for (String c : cf.getKeys(false)) {
                if (commandMap.containsKey(c) || cf.getBoolean(c + ".is-alias"))
                    continue;

                CommandWrapper cw = new CommandWrapper(cf.getConfigurationSection(c));
                commandList.add(cw);
                commandMap.put(c, cw);
                for (String ca : cw.getAliases())
                    commandMap.put(ca, cw);
            }

            commandList.sort(Comparator.comparing(CommandWrapper::getName));

            this.reload = false;
            plugin.getLogger().info("Reloaded help menu commands");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().warning("Help menu will not work properly. Update plugin.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        loadCommands();

        boolean showDetails = sender.hasPermission("arcane.command.help.details");

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
            for (int i = (page - 1) * 7; i < pg; i++) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, cmdList.get(i).getUsageForList(showDetails));
                else
                    sender.spigot().sendMessage(cmdList.get(i).getUsageForList(showDetails));
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
        } catch (NumberFormatException ignored) {}

        // Has argument and not a number

        CommandWrapper cw = commandMap.get(args[0]);

        // Command DNE
        if (cw == null) {
            if (sender instanceof Player)
                ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, notFoundMsg);
            else
                sender.spigot().sendMessage(notFoundMsg);
            return true;
        }

        // Send help topic
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ArcaneText.usage(cw.getUsage()));
        } else {
            sender.spigot().sendMessage(ArcaneText.usage(cw.getUsage()));
        }

        // Send Description if exists
        if (!cw.getDescription().isEmpty()) {
            BaseComponent desc = new TextComponent(" Description: ");
            desc.addExtra(cw.getDescription());
            desc.setColor(ChatColor.RED);

            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, desc);
            } else {
                sender.spigot().sendMessage(desc);
            }
        }

        // Send Aliases if exists
        if (cw.getAliases().length != 0) {
            BaseComponent desc = new TextComponent(cw.getAliases().length > 1 ? " Aliases: " : " Alias: ");
            desc.addExtra(String.join(", ", cw.getAliases()));
            desc.setColor(ChatColor.RED);

            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, desc);
            } else {
                sender.spigot().sendMessage(desc);
            }
        }

        // Has permission to see extra details
        if (showDetails) {
            if (cw.getPermission() != null) {
                BaseComponent perm = new TextComponent(" Origin:");
                perm.addExtra(cw.getOrigin());
                perm.addExtra(", Permission: ");
                perm.addExtra(cw.getPermission());
                perm.setColor(ChatColor.GRAY);
                perm.setItalic(true);

                if (sender instanceof Player) {
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, perm);
                } else {
                    sender.spigot().sendMessage(perm);
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        loadCommands();

        if (args.length == 0) {
            return new ArrayList<>(commandMap.keySet());
        }
        if (args.length == 1) {
            List<String> ret = new ArrayList<>();

            for (String cs : commandMap.keySet())
                if (cs.startsWith(args[0]))
                    ret.add(cs);

            return ret;
        }
        return Collections.emptyList();
    }


    private List<CommandWrapper> getCommands(CommandSender sender) {
        List<CommandWrapper> ret = new ArrayList<>();

        for (CommandWrapper c : commandList) {
            if (!c.hasPermission(sender))
                continue;
            ret.add(c);
        }

        return ret;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void pluginEnableEvent(PluginEnableEvent e) {
        reload = true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void pluginDisableEvent(PluginDisableEvent e) {
        reload = true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void commandTabEvent(TabCompleteEvent e) {
        loadCommands();

        String cmd = e.getBuffer().toLowerCase();
        CommandSender p = e.getSender();

        if (p.isOp() || !cmd.startsWith("/") || cmd.contains(" "))
            return;

        List<String> list = e.getCompletions();

        // Check hide colon for this player
        if (p.hasPermission("arcane.tabcomplete.hidecolon"))
            list.removeIf(s -> s.contains(":"));

        // Remove commands player has no permission for
        for (Map.Entry<String, CommandWrapper> c : commandMap.entrySet()) {
            if (c.getValue().getPermission() != null && !p.hasPermission(c.getValue().getPermission())) {
                list.remove("/" + c.getKey());
            }
        }

        // Add BungeeCord commands
        for (BungeeCommandUsage cw : BungeeCommandUsage.values()) {
            String c = cw.getCommand();
            if (!list.contains(c) && (cw.getPermission() == null || p.hasPermission(cw.getPermission()))) {
                // Add main command
                if (c.startsWith(cmd))
                    list.add(c);
                // Add aliases
                if (cw.getAliases() != null) {
                    for (String a : cw.getAliases()) {
                        String ca = "/" + a;
                        if (ca.startsWith(cmd))
                            list.add(ca);
                    }
                }
            }
        }
    }

    private final class CommandWrapper {
        private final String name;
        private final String usage;
        private final ClickEvent clickEvent;
        private final String permission;
        private final BaseComponent origin;
        private final String description;
        private final String[] aliases;

        private CommandWrapper(ConfigurationSection cs) {
            this.name = cs.getName();
            this.origin = new TextComponent("(Config)");
            __originFormatting();
            this.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + name + " ");

            this.permission = cs.getString("permission");
            this.usage = cs.getString("usage",
                    "/" + name);
            this.description = cs.getString("description", "");
            this.aliases = cs.getStringList("aliases").toArray(new String[0]);
            Arrays.sort(this.aliases);
        }

        private CommandWrapper(Command command, ConfigurationSection cs) {
            this.name = command.getName();
            this.origin = new TextComponent((command instanceof PluginCommand
                    ? ((PluginCommand) command).getPlugin().getName()
                    : command instanceof BukkitCommand
                    ? "Bukkit"
                    : "(Unknown)") + (cs == null ? "" : " (+Config)"));
            __originFormatting();
            this.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + name + " ");

            if (cs == null) {
                this.permission = command.getPermission();
                this.usage = command.getUsage().equals("") ? "/" + command.getName() : command.getUsage();
                this.description = command.getDescription();
                this.aliases = command.getAliases().toArray(new String[0]);
                return;
            }

            this.permission = cs.getString("permission", command.getPermission());
            this.usage = cs.getString("usage",
                    command.getUsage().equals("") ? "/" + name : command.getUsage());
            this.description = cs.getString("description", command.getDescription());
            List<String> list = new ArrayList<>(command.getAliases());
            list.addAll(cs.getStringList("aliases")); // Merge aliases
            this.aliases = list.toArray(new String[0]);
            Arrays.sort(this.aliases);
        }

        private CommandWrapper(BungeeCommandUsage command, ConfigurationSection cs) {
            this.name = command.getName();
            this.origin = new TextComponent("(BungeeCord" + (cs == null ? ")" : "  +Config)"));
            __originFormatting();
            this.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + name + " ");

            if (cs == null) {
                this.permission = command.getPermission();
                this.usage = command.getUsage();
                this.description = command.getDescription();
                this.aliases = command.getAliases();
                return;
            }

            this.permission = cs.getString("permission", command.getPermission());
            this.usage = cs.getString("usage",
                    command.getUsage().equals("") ? "/" + name : command.getUsage());
            this.description = cs.getString("description", command.getDescription());
            List<String> list = new ArrayList<>(Arrays.asList(command.getAliases()));
            list.addAll(cs.getStringList("aliases")); // Merge aliases
            this.aliases = list.toArray(new String[0]);
            Arrays.sort(this.aliases);
        }

        private void __originFormatting() {
            this.origin.setColor(ChatColor.GRAY);
            this.origin.setItalic(true);
        }

        private String getName() {
            return name;
        }

        private BaseComponent getUsageForList(boolean showDetails) {
            BaseComponent ret = usage.startsWith("commands.") ? new TranslatableComponent(usage) : new TextComponent(usage);
            ComponentBuilder cb = null;
            if (!description.isEmpty())
                cb = new ComponentBuilder(description);

            if (showDetails) {
                ret.addExtra(" ");
                ret.addExtra(origin);
                if (permission != null) {
                    BaseComponent perm = new TextComponent(" Permission: " + permission);
                    perm.setColor(ChatColor.GRAY);
                    perm.setItalic(true);
                    if (cb == null) {
                        cb = new ComponentBuilder(perm);
                    } else {
                        cb.append("\n");
                        cb.append(perm);
                    }
                }
            }

            if (cb != null)
                ret.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, cb.create()));

            ret.setClickEvent(clickEvent);
            return ret;
        }

        private String getUsage() {
            return usage;
        }

        private String getDescription() {
            return description;
        }

        private BaseComponent getOrigin() {
            return origin;
        }

        private String getPermission() {
            return permission;
        }

        private String[] getAliases() {
            return aliases;
        }

        private boolean hasPermission(CommandSender sender) {
            return permission == null || sender.hasPermission(permission);
        }
    }
}
