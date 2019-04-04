package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {
    private final ArcaneServer plugin;

    PlayerEvents(ArcaneServer plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chatEvent(AsyncPlayerChatEvent e) {
        if (e.isCancelled())
            return;

        BaseComponent send = new TranslatableComponent("chat.type.text", ArcaneText.playerComponentSpigot(e.getPlayer()),
                ArcaneText.url(e.getMessage()));

        try {
            // Check if LuckPerms exists
            Class.forName( "me.lucko.luckperms.LuckPerms" );
            LuckPermsApi lpApi = LuckPerms.getApi();
            User u = lpApi.getUser(e.getPlayer().getUniqueId());
            String tag = null;

            if (u != null) {
                MetaData metaData = u.getCachedData().getMetaData(Contexts.global());
                try {
                    int index = Integer.valueOf(metaData.getMeta().get("PrefixPriority"));
                    if (index != -1)
                        tag = metaData.getPrefixes().get(index);
                } catch (NumberFormatException ignored) {
                    tag = metaData.getPrefix();
                }

                if (tag != null) {
                    tag = ChatColor.translateAlternateColorCodes('&', tag);
                    e.setFormat(tag + ChatColor.RESET + " " + e.getFormat());

                    BaseComponent chat = send;
                    send = new TextComponent();
                    for (BaseComponent tp : TextComponent.fromLegacyText(tag))
                        send.addExtra(tp);
                    send.addExtra(" ");
                    send.addExtra(chat);
                }
            }

            plugin.getPluginMessenger().chat(e.getPlayer(), e.getMessage(), tag);
        } catch (ClassNotFoundException ignored) {}

        for (Player p : e.getRecipients())
            p.spigot().sendMessage(ChatMessageType.CHAT, send);

        e.getRecipients().clear(); // Destroy default event.
    }


    // Join/Leave events are covered on the proxy.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void joinEvent(PlayerJoinEvent e) {
        e.setJoinMessage("");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void leaveEvent(PlayerQuitEvent e) {
        e.setQuitMessage("");
    }
}
