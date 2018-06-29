package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class PluginMessenger implements PluginMessageListener {
    private final ArcaneServer plugin;
    private final boolean xRayAlert;
    private final boolean signAlert;
    private String serverName = "(unknown)";

    PluginMessenger(ArcaneServer plugin) {
        this.plugin = plugin;
        this.xRayAlert = plugin.getConfig().getBoolean("spy.xray-alert");
        this.signAlert = plugin.getConfig().getBoolean("spy.sign-alert");

        // Get serverName
        // TODO: Make this work (requires player to join: Player Join Event?)
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer"); // So BungeeCord knows to forward it
        plugin.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    void chat(Player p, String msg) {
        chat(p.getName(), p.getDisplayName(), p.getUniqueId().toString(), msg, p);
    }

    void chat(String name, String displayName, String uuid, String msg, Player pluginMessageSender) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward"); // So BungeeCord knows to forward it
        out.writeUTF("ONLINE");

        // If ArcaneLog is null: another server is main (server) server. ChatAndLog.
        out.writeUTF(plugin.getServer().getPluginManager().getPlugin("ArcaneLog") == null ? "ChatAndLog" : "Chat"); // Subchannel Chat

        ByteArrayOutputStream byteos = new ByteArrayOutputStream();
        try (DataOutputStream os = new DataOutputStream(byteos)) {

            os.writeUTF(serverName);
            os.writeUTF(msg);
            os.writeUTF(name);
            os.writeUTF(displayName == null ? name : displayName);
            os.writeUTF(uuid == null ? "" : uuid);

            out.writeShort(byteos.toByteArray().length);
            out.write(byteos.toByteArray());
            pluginMessageSender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
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

                // TODO: Prefix stuff (from LuckPerms using uuid)
                TranslatableComponent chat = new TranslatableComponent("chat.type.text", ArcaneText.playerComponent(name, displayName, uuid, "Server: " + server), msg);

                for (Player p : plugin.getServer().getOnlinePlayers())
                    p.spigot().sendMessage(ChatMessageType.CHAT, chat);

                plugin.getServer().getConsoleSender().sendMessage("*" + chat.toPlainText());

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
