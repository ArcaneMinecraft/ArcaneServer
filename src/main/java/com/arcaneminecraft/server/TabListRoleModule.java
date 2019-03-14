package com.arcaneminecraft.server;

import com.arcaneminecraft.server.ArcaneServer;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabListRoleModule implements Listener {

    TabListRoleModule(ArcaneServer plugin) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            addRole(p);
        }
    }

    private void addRole(Player p) {
        LuckPermsApi lpApi = LuckPerms.getApi();
        User u = lpApi.getUser(p.getUniqueId());

        if (u != null) {
            String role = u.getCachedData().getMetaData(Contexts.global()).getMeta().get("TabListRole");
            if (role != null) {
                role = ChatColor.translateAlternateColorCodes('&', role);
                p.setPlayerListName(p.getName() + ChatColor.GRAY + " | " + role);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void joinAddRole(PlayerJoinEvent e) {
        addRole(e.getPlayer());
    }
}
