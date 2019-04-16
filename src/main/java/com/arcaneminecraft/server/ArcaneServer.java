package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneColor;
import com.arcaneminecraft.server.command.*;
import com.arcaneminecraft.server.listener.AlertListener;
import com.arcaneminecraft.server.listener.BuildPermissionListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public final class ArcaneServer extends JavaPlugin {
    private static ArcaneServer instance;

    private ArcAFKCommand arcAFK;
    private PluginMessenger pluginMessenger;

    public static ArcaneServer getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        ArcaneServer.instance = this;

        saveDefaultConfig();

        // Register with BungeeCord plugin message
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.pluginMessenger = new PluginMessenger(this); // Above must come before this.
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pluginMessenger);
        getServer().getPluginManager().registerEvents(pluginMessenger, this);

        if (getServer().getOnlinePlayers().size() > 0) {
            getServer().getScheduler().runTaskLaterAsynchronously(this,
                    this.pluginMessenger.new getServerName(getServer().getOnlinePlayers().iterator().next()),
                    1);
        }

        // X-Ray and other notification alert
        getServer().getMessenger().registerOutgoingPluginChannel(this, "arcaneserver:alert");
        getServer().getPluginManager().registerEvents(new AlertListener(this), this);

        // General stuff
        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
        getServer().getPluginManager().registerEvents(new BuildPermissionListener(), this);

        // Tab List Modifying Events
        getLogger().info(
                "Tab list: modify-tablist: "
                + getConfig().getBoolean("modify-tablist", false)
        );
        if (getConfig().getBoolean("modify-tablist", false))
            getServer().getPluginManager().registerEvents(new TabListRoleModule(this), this); // this must come before AFK

        // Commands
        LocalChatCommands lc = new LocalChatCommands(this);
        HelpCommand hc = new HelpCommand(this);
        SpawnCommand sc = new SpawnCommand(this);

        if (getConfig().getBoolean("localchat.enabled", true)) {
            getCommand("local").setExecutor(lc);
            getCommand("localtoggle").setExecutor(lc);
            getCommand("localrange").setExecutor(lc);
            getServer().getPluginManager().registerEvents(lc, this);
        }
        getCommand("global").setExecutor(lc);
        getCommand("help").setExecutor(hc);
        getServer().getPluginManager().registerEvents(hc, this);
        if (getConfig().getBoolean("spawn.command-enabled", false)) {
            getCommand("spawn").setExecutor(sc);
        }
        getCommand("setworldspawn").setExecutor(sc);
        if (getConfig().getBoolean("spawn.listener-enabled", false))
            getServer().getPluginManager().registerEvents(sc, this);

        this.arcAFK = new ArcAFKCommand(this);
        getCommand("afk").setExecutor(arcAFK);
        getServer().getPluginManager().registerEvents(arcAFK, this);

        this.sleepListner = new SleepDayListener(this);
        getServer().getPluginManager().registerEvents(sleepListner, this);

        GmCommands gm = new GmCommands();
        getCommand("gms").setExecutor(gm);
        getCommand("gmc").setExecutor(gm);
        getCommand("gma").setExecutor(gm);
        getCommand("gmsp").setExecutor(gm);
        getCommand("kill").setExecutor(new KillCommand());
        getCommand("uuid").setExecutor(new UuidCommand());
        getCommand("opme").setExecutor(new OpmeCommand());
        getCommand("diamondstoneratio").setExecutor(new DiamondStoneRatioCommand());
        getCommand("killdeathratio").setExecutor(new KillDeathRatioCommand());
    }

    @Override
    public void onDisable() {
        arcAFK.onDisable();
        // List roles
        for (Player p : getServer().getOnlinePlayers()) {
            p.setPlayerListName(p.getName());
        }

        this.arcAFK = null;
        this.pluginMessenger = null;
        this.sleepListner = null;
        ArcaneServer.instance = null;
    }

    public PluginMessenger getPluginMessenger() {
        return pluginMessenger;
    }

    public SleepDayListener getSleepListener() {return sleepListner;}

    public boolean isAFK(Player p) {
        return arcAFK.isAFK(p);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(ArcaneColor.CONTENT + "Command '/" + label + "' is disabled in this world");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
