package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ArcaneText;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class PluginMessenger implements PluginMessageListener {
    private final ArcaneServer plugin;

    PluginMessenger(ArcaneServer plugin) {
        this.plugin = plugin;
    }

    void chat(Player p, String msg) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward"); // So BungeeCord knows to forward it
            out.writeUTF("ONLINE");
            // ArcaneLog is null: another server is main (server) server. ChatAndLog.
            out.writeUTF(plugin.getServer().getPluginManager().getPlugin("ArcaneLog") == null ? "ChatAndLog" : "Chat"); // Subchannel Chat

            ByteArrayOutputStream byteos = new ByteArrayOutputStream();
            DataOutputStream os = new DataOutputStream(byteos);
            os.writeUTF(msg);
            os.writeUTF(p.getName());
            os.writeUTF(p.getDisplayName());
            os.writeUTF(p.getUniqueId().toString());

            out.writeShort(byteos.toByteArray().length);
            out.write(byteos.toByteArray());
            p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord"))
            return;

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subChannel = in.readUTF();

            if (subChannel.equals("Chat") || subChannel.equals("ChatAndLog")) {
                byte[] msgBytes = new byte[in.readShort()];
                in.readFully(msgBytes);

                DataInputStream is = new DataInputStream(new ByteArrayInputStream(msgBytes));
                String msg = is.readUTF();
                String name = is.readUTF();
                String displayName = is.readUTF();
                String uuid = is.readUTF();

                // TODO: Prefix stuff (from LuckPerms using uuid)
                TranslatableComponent chat = new TranslatableComponent("chat.type.text", ArcaneText.playerComponent(name, displayName, uuid), msg);

                for (Player p : plugin.getServer().getOnlinePlayers())
                    p.spigot().sendMessage(ChatMessageType.CHAT, chat);

                plugin.getLogger().info(chat.toLegacyText());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
