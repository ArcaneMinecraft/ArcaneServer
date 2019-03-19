package com.arcaneminecraft.server.command;

import com.arcaneminecraft.api.ArcaneColor;
import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.BungeeCommandUsage;
import com.arcaneminecraft.server.ArcaneServer;
import com.arcaneminecraft.server.SpigotLocaleTool;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class HelpCommand implements TabExecutor, Listener {
    private final ArcaneServer plugin;
    private final BaseComponent notFoundMsg;
    private final SimpleCommandMap commandMap;
    private List<CommandWrapper> commandList;
    private Map<String, CommandWrapper> nameToCommandMap;
    private boolean reload;


    public HelpCommand(ArcaneServer plugin) {
        this.plugin = plugin;
        this.notFoundMsg = new TranslatableComponent("commands.help.failed");
        this.notFoundMsg.setColor(ArcaneColor.NEGATIVE);

        SimpleCommandMap commandMap = null;
        try {
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);

            commandMap = (SimpleCommandMap) f.get(plugin.getServer().getPluginManager());

        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().log(Level.WARNING, "Help menu will not work properly. Update plugin.", e);
        }
        this.commandMap = commandMap;

        // Load commands after server finishes loading plugins
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            this.reload = true;
            this.loadCommands();
        }, 1L);
    }

    private void loadCommands() {
        if (!reload || commandMap == null)
            return;

        commandList = new ArrayList<>();
        nameToCommandMap = new HashMap<>();

        ConfigurationSection cf = plugin.getConfig().getConfigurationSection("help-override.commands");
        if (cf == null) {
            plugin.getLogger().warning("'help-override.commands' does not exist in config.yml. /help menu will not work.");
            return;
        }

        // First: BungeeCord Commands
        for (BungeeCommandUsage c : BungeeCommandUsage.values()) {
            if (cf.getBoolean(c.getName() + ".is-alias"))
                continue;

            CommandWrapper cw = new CommandWrapper(c, cf.getConfigurationSection(c.getName()));
            commandList.add(cw);
            nameToCommandMap.put(c.getName(), cw);
            for (String ca : cw.getAliases())
                nameToCommandMap.put(ca, cw);
        }

        // Second: Registered commands
        for (Command c : commandMap.getCommands()) {

            // No Permission Message Hack
            c.setPermissionMessage(ArcaneColor.NEGATIVE + "Unknown command or insufficient permissions");

            if (nameToCommandMap.containsKey(c.getName()) || cf.getBoolean(c.getName() + ".is-alias"))
                continue;

            // Forced Permission Hack
            ConfigurationSection cs = cf.getConfigurationSection(c.getName());
            String perm = null;
            if (cs != null) {
                perm = cs.getString("permission");
                if (perm != null) {
                    c.setPermission(perm);
                    plugin.getLogger().info("Set permission " + c.getPermission() + " to " + c.getName());
                }
            }

            CommandWrapper cw = new CommandWrapper(c, cs);
            commandList.add(cw);
            nameToCommandMap.put(c.getName(), cw);

            for (String ca : cw.getAliases()) {
                if (perm != null) {
                    Command cmda = commandMap.getCommand(ca);
                    if (cmda != null && cmda.getPermission() == null) {
                        cmda.setPermission(perm);
                        plugin.getLogger().info("Added permission " + cmda.getPermission() + " to " + cmda.getName());
                    }
                }
                nameToCommandMap.put(ca, cw);
            }
        }

        // Third: Remaining config.yml Commands
        for (String c : cf.getKeys(false)) {
            if (nameToCommandMap.containsKey(c) || cf.getBoolean(c + ".is-alias"))
                continue;

            CommandWrapper cw = new CommandWrapper(cf.getConfigurationSection(c));
            commandList.add(cw);
            nameToCommandMap.put(c, cw);
            for (String ca : cw.getAliases())
                nameToCommandMap.put(ca, cw);
        }

        commandList.sort(Comparator.comparing(CommandWrapper::getName));

        reload = false;
        plugin.getLogger().info("Reloaded help menu commands");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        loadCommands();

        boolean showDetails = sender.hasPermission("arcane.command.help.details");

        Locale locale;
        if (sender instanceof Player) {
            locale = SpigotLocaleTool.parse(((Player) sender).getLocale());
        } else {
            locale = null;
        }

        try {
            // Help menu
            int page = args.length == 0 ? 1 : Integer.parseInt(args[0]);

            // Get commands
            List<CommandWrapper> cmdList = getCommands(sender);
            // 7 entries per page
            int totalPages = (cmdList.size() - 1) / 7 + 1;

            BaseComponent oor = ArcaneText.numberOutOfRange(page, 0, totalPages);
            if (oor != null) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, oor);
                else
                    sender.spigot().sendMessage(oor);
                return true;
            }

            int pg = Math.min(cmdList.size(), page * 7);

            // First line
            BaseComponent header = ArcaneText.translatable(locale, "commands.help.header", page, totalPages);
            header.setColor(ArcaneColor.LIST);
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
                BaseComponent footer = ArcaneText.translatable(locale, "commands.help.tip");
                footer.setColor(ArcaneColor.LIST_VARS);
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, footer);
                else
                    sender.spigot().sendMessage(footer);
            }


            return true;
        } catch (NumberFormatException ignored) {}

        // Has argument and not a number

        CommandWrapper cw = nameToCommandMap.get(args[0]);

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
            BaseComponent desc = new TextComponent(" ");
            desc.addExtra(ArcaneText.translatable(locale, "commands.help.description", cw.getDescription()));
            desc.setColor(ArcaneColor.NEGATIVE);

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
            desc.setColor(ArcaneColor.NEGATIVE);

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
                perm.setColor(ArcaneColor.CONTENT);
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
            return new ArrayList<>(nameToCommandMap.keySet());
        }
        if (args.length == 1) {
            List<String> ret = new ArrayList<>();

            for (String cs : nameToCommandMap.keySet())
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
    public void onPlayerJoin(PlayerJoinEvent e) {
        loadCommands();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent e) {
        reload = true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent e) {
        reload = true;
        loadCommands();
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

                String usage = command.getUsage();

                this.usage = usage.isEmpty()
                        ? "/" + name
                        : (usage.startsWith("/") ? "" : "/") + usage.replace("<command>", name);
                this.description = (command instanceof BukkitCommand) ? "" : command.getDescription();
                this.aliases = command.getAliases().toArray(new String[0]);
                return;
            }

            this.permission = cs.getString("permission", command.getPermission());
            this.usage = cs.getString("usage",
                    command.getUsage().equals("") ? "/" + name : command.getUsage());
            this.description = cs.getString("description", (command instanceof BukkitCommand) ? "" : command.getDescription());
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
            this.origin.setColor(ArcaneColor.CONTENT);
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
                    perm.setColor(ArcaneColor.CONTENT);
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
