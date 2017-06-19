package com.arcaneminecraft;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionBarAPI {
	public static void sendMessage(Player p, String msg) {
        Object handle, connection, component, packet;
		try {
			handle = p.getClass().getMethod("getHandle").invoke(p);
			connection = handle.getClass().getField("playerConnection").get(handle);
			component = getMinecraftClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class).invoke(null, "{\"text\": \"" +
			        ChatColor.translateAlternateColorCodes('&', msg) + "\"}");
			packet = getMinecraftClass("PacketPlayOutChat").getConstructor(getMinecraftClass("IChatBaseComponent"), byte.class).newInstance(component, (byte) 2);
			
	        connection.getClass().getMethod("sendPacket", getMinecraftClass("Packet")).invoke(connection, packet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | NoSuchFieldException | InstantiationException | ClassNotFoundException e) {
			Bukkit.getLogger().warning("[ArcaneSurvival] Failed to send ActionBar to Player " + p.getName() + " regarding greylist status.");
			e.printStackTrace();
		}
	}
	
	private static Class<?> getMinecraftClass(String c) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server."
				+ Bukkit.getServer().getClass().getPackage().getName().substring(23)
				+ "." + c);
	}
}
