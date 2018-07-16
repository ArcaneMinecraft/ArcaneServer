package com.arcaneminecraft.server;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.HashMap;
import java.util.logging.Level;

public class Alert implements Listener {
    private final ArcaneServer plugin;
    private final HashMap<Material, XRayCheck> xrayList;

    Alert(ArcaneServer plugin) {
        this.plugin = plugin;

        this.xrayList = new HashMap<>();
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("spy.xray-blocks");

        // TODO: Check for null cs

        for (String key : cs.getKeys(false)) {
            new XRayCheck(cs.getConfigurationSection(key));
        }
    }

    private final class XRayCheck {
        private final int yMax;
        private final int yMin;
        private final Biome biome;

        private XRayCheck(ConfigurationSection cs) {
            this.yMax = cs.getInt("y-max", 256);
            this.yMin = cs.getInt("y-min", 0);
            String csBiome = cs.getString("biome"); // TODO: Define many biomes?
            Biome b = null;
            try {
                b = csBiome == null ? null : Biome.valueOf(csBiome.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Configuration error detected", e);
            }
            this.biome = b;

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
        // Check if sign is empty. If at least one line has something: send alert.
        for (String s : e.getLines()) {
            if (!s.isEmpty()) {
                plugin.getPluginMessenger().signAlert(e.getPlayer(), e.getBlock(), e.getLines());
                return;
            }
        }
    }
}
