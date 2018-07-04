package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.arcaneminecraft.api.ArcaneColor;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class PluginMessenger implements PluginMessageListener, Listener {
    private final ArcaneServer plugin;
    private final boolean xRayAlert;
    private final boolean signAlert;
    private String serverName = "(unknown)";

    PluginMessenger(ArcaneServer plugin) {
        this.plugin = plugin;
        this.xRayAlert = plugin.getConfig().getBoolean("spy.xray-alert");
        this.signAlert = plugin.getConfig().getBoolean("spy.sign-alert");
    }

    // Event to fetch server name
    @EventHandler(priority = EventPriority.LOWEST)
    public void usingPluginMessage(PlayerJoinEvent e) {
        if (plugin.getServer().getOnlinePlayers().size() == 1) {
            Player p = e.getPlayer();
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    if (!p.isOnline()) {
                        // If player is not yet in game, it's impossible to send a plugin message.
                        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, this, 10);
                        return;
                    }
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("GetServer"); // So BungeeCord knows to forward it
                    p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

                }
            }, 1);
        }
    }

    void chat(Player p, String msg, String tag) {
        chat(p.getName(), p.getDisplayName(), p.getUniqueId().toString(), msg, tag, p);
    }

    void chat(String name, String displayName, String uuid, String msg, String tag, Player pluginMessageSender) {
        String channel =
                // If ArcaneLog is null: another server is main (server) server. If uuid is not null: able to log.
                uuid != null && plugin.getServer().getPluginManager().getPlugin("ArcaneLog") == null
                        ? "ChatAndLog" : "Chat";

        ByteArrayOutputStream byteos = new ByteArrayOutputStream();
        try (DataOutputStream os = new DataOutputStream(byteos)) {

            os.writeUTF(serverName);
            os.writeUTF(msg);
            os.writeUTF(name);
            os.writeUTF(displayName == null ? name : displayName);
            os.writeUTF(uuid == null ? "" : uuid);
            os.writeUTF(tag == null ? "" : tag);

            forwardChannelMessage(channel, byteos, pluginMessageSender); // Subchannel
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void afk(Player p, boolean isAFK) {
        String channel = "AFK";

        ByteArrayOutputStream byteos = new ByteArrayOutputStream();
        try (DataOutputStream os = new DataOutputStream(byteos)) {
            os.writeUTF(serverName);
            os.writeUTF(p.getName());
            os.writeUTF(p.getDisplayName());
            os.writeUTF(p.getUniqueId().toString());
            os.writeBoolean(isAFK);

            forwardChannelMessage(channel, byteos, p); // Subchannel
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void forwardChannelMessage(String channel, ByteArrayOutputStream byteArrayOutputStream, Player pluginMessageSender) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward"); // So BungeeCord knows to forward it
        out.writeUTF("ONLINE"); // Target server
        out.writeUTF(channel); // Subchannel

        out.writeShort(byteArrayOutputStream.toByteArray().length);
        out.write(byteArrayOutputStream.toByteArray());
        pluginMessageSender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    void xRayAlert(Player p, Block b) {
        if (xRayAlert)
            ArcaneAlertChannel(p, "XRay", b, b.getType().toString());
    }

    void signAlert(Player p, Block b, String[] l) {
        if (signAlert)
            ArcaneAlertChannel(p, "Sign", b, l);
    }

    private void ArcaneAlertChannel(Player p, String type, Block b, String... data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(serverName);
        out.writeUTF(type);
        out.writeUTF(p.getName());
        out.writeUTF(p.getUniqueId().toString());
        out.writeUTF(b.getWorld().getName());
        out.writeInt(b.getX());
        out.writeInt(b.getY());
        out.writeInt(b.getZ());

        for (String s : data) {
            out.writeUTF(s);
        }

        p.sendPluginMessage(plugin, "ArcaneAlert", out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord"))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();

        if (subChannel.equals("Chat") || subChannel.equals("ChatAndLog")) {
            byte[] msgBytes = new byte[in.readShort()];
            in.readFully(msgBytes);

            try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(msgBytes))) {
                String server = is.readUTF();
                String msg = is.readUTF();
                String name = is.readUTF();
                String displayName = is.readUTF();
                String uuid = is.readUTF();
                String tag = is.readUTF();

                BaseComponent chat = new TranslatableComponent("chat.type.text",
                        ArcaneText.playerComponent(name, displayName, uuid, "Server: " + server), ArcaneText.url(msg));
                BaseComponent send;

                if (tag.isEmpty()) {
                    send = chat;
                } else {
                    BaseComponent t = new TextComponent(tag);
                    send = new TextComponent();
                    send.addExtra(t);
                    send.addExtra(" ");
                    send.addExtra(chat);
                }

                for (Player p : plugin.getServer().getOnlinePlayers())
                    p.spigot().sendMessage(ChatMessageType.CHAT, send);

                plugin.getServer().getConsoleSender().sendMessage(server + ": " + send.toPlainText());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        if (subChannel.equals("AFK")) {
            byte[] msgBytes = new byte[in.readShort()];
            in.readFully(msgBytes);

            try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(msgBytes))) {

                String server = is.readUTF();
                String name = is.readUTF();
                String displayName = is.readUTF();
                String uuid = is.readUTF();
                boolean isAFK = is.readBoolean();

                BaseComponent send = plugin.getArcAFK().formatAFK(
                        ArcaneText.playerComponent(name, displayName, uuid, "Server: " + server),
                        "is " + (isAFK ? "now" : "no longer") + " AFK"
                );
                send.setColor(ArcaneColor.CONTENT);

                for (Player p : plugin.getServer().getOnlinePlayers())
                    p.spigot().sendMessage(ChatMessageType.SYSTEM, send);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        if (subChannel.equals("GetServer")) {
            this.serverName = in.readUTF();
            plugin.getLogger().info("Server name set as: " + this.serverName);
        }
    }
}
