package com.arcaneminecraft.server;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.HashMap;

public class Alert implements Listener {
    private final ArcaneServer plugin;
    private final HashMap<Material, XRayCheck> xrayList;

    Alert(ArcaneServer plugin) {
        this.plugin = plugin;

        this.xrayList = new HashMap<>();
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("spy.xray-blocks");

        // TODO: Check for null cs

        for (String key : cs.getKeys(false))
            new XRayCheck(cs.getConfigurationSection(key));
    }

    private final class XRayCheck {
        private final int yMax;
        private final int yMin;
        private final Biome biome;

        private XRayCheck(ConfigurationSection cs) {
            this.yMax = cs.getInt("y-max", 256);
            this.yMin = cs.getInt("y-min", 0);
            String csBiome = cs.getString("biome"); // TODO: Define many biomes?
            this.biome = csBiome == null ? null : Biome.valueOf(csBiome.toUpperCase());

            // cs.getName() (item name) must be all uppercase for some reason
            xrayList.put(Material.getMaterial(cs.getName().toUpperCase()), this);
        }

        private boolean toLog(Block b) {
            return (biome == null || biome.equals(b.getBiome())) && b.getY() <= yMax && b.getY() >= yMin;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOreMined(BlockBreakEvent e) {
        XRayCheck check = xrayList.get(e.getBlock().getType());

        if (check != null && check.toLog(e.getBlock())) {
            plugin.getPluginMessenger().xRayAlert(e.getPlayer(), e.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSign(SignChangeEvent e) {
        plugin.getPluginMessenger().signAlert(e.getPlayer(), e.getBlock(), e.getLines());
    }
}
