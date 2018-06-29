package com.arcaneminecraft.server;

import com.arcaneminecraft.api.ColorPalette;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.FlowerPot;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Redstone;


// TODO: When Spigot 1.13 API is published, review all the actions that should be further restricted.
public class Greylist implements Listener {
    private static final String TRUSTED_PERMISSION = "arcane.build";

    // Sends message through Action Bar (bar above item bar)
    private void noPerm(Cancellable e, Player p) {
        if (p.hasPermission(TRUSTED_PERMISSION)) {
            return;
        }

        e.setCancelled(true);

        BaseComponent not = new TextComponent("not");
        BaseComponent apply = new TextComponent("/apply");
        not.setColor(ColorPalette.NEGATIVE);
        apply.setColor(ColorPalette.POSITIVE);

        BaseComponent msg = new TextComponent("You do ");
        msg.addExtra(not);
        msg.addExtra(" have build permissions! Apply for it via ");
        msg.addExtra(apply);
        msg.addExtra("!");

        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }

    // Player and Entity
    @EventHandler(priority = EventPriority.HIGHEST)
    public void armorStandManipulate(PlayerArmorStandManipulateEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void bucket(PlayerBucketEmptyEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void bucket(PlayerBucketFillEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void shearEntity(PlayerShearEntityEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void unleashEntity(PlayerUnleashEntityEvent e) {
        noPerm(e, e.getPlayer());
    }

    // Advanced Player and Entity
    @EventHandler(priority = EventPriority.HIGHEST)
    public void interactEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission(TRUSTED_PERMISSION))
            return;

        if (e.getRightClicked() instanceof Hanging) // Item Frame and paintings
            noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hangingBreakByEntity(HangingBreakByEntityEvent e) {
        Player p;
        if (!(e.getRemover() instanceof Player && !(p = (Player) e.getRemover()).hasPermission(TRUSTED_PERMISSION)))
            return;

        noPerm(e, p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void damageByEntity(EntityDamageByEntityEvent e) {
        Player p;
        if (!(e.getDamager() instanceof Player && !(p = (Player) e.getDamager()).hasPermission(TRUSTED_PERMISSION)))
            return;

        Entity d = e.getEntity();

        // They can always hit Monsters + Slimes
        if (d instanceof Monster || d instanceof Slime)
            return;

        // They cannot hit important items
        if (
                d instanceof InventoryHolder || // Horses, other players
                        d instanceof Hanging || // Item Frame and paintings
                        d instanceof ArmorStand // Armor Stands
                )
            noPerm(e, p);
    }

    // Block
    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBreakEvent e) {
        noPerm(e, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockPlace(BlockPlaceEvent e) {
        noPerm(e, e.getPlayer());
    }

    // Advanced Block
    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission(TRUSTED_PERMISSION))
            return;

        Action a = e.getAction();
        Block b = e.getClickedBlock();

        if (b == null)
            return;

        BlockState s = b.getState();
        Material m = e.getMaterial();

        if (
                a == Action.PHYSICAL || // No Automatic Redstone Triggers
                        (a == Action.RIGHT_CLICK_BLOCK && (
                                s.getData() instanceof Redstone || // No Redstone device clicks
                                        s instanceof FlowerPot || // because Flower Pot
                                        (m != null && ( // Prevent placement of entities below
                                                m == Material.ITEM_FRAME ||
                                                        m == Material.ARMOR_STAND ||
                                                        m == Material.PAINTING
                                        ))
                        ))
                )
            noPerm(e, e.getPlayer());
    }

    // Inventory
    @EventHandler(priority = EventPriority.HIGHEST)
    public void inventoryClick(InventoryClickEvent e) {
        Player p;
        if (!(e.getWhoClicked() instanceof Player && !(p = (Player) e.getWhoClicked()).hasPermission(TRUSTED_PERMISSION)))
            return;

        Inventory i = e.getInventory();
        InventoryType it = i.getType();

        // TODO: Allow crafting table, enchantment table, Ender Chest, and anything else that doesn't affect other players.
        if (
                it == InventoryType.PLAYER ||
                        it == InventoryType.CRAFTING ||
                        it == InventoryType.WORKBENCH ||
                        it == InventoryType.ENDER_CHEST ||
                        it == InventoryType.ENCHANTING ||
                        it == InventoryType.MERCHANT)
            return;

        InventoryAction a = e.getAction();

        // If item ends up in different inventory, block it.
        // Caveat: If holding item on cursor, spamming it enough will make it pass through.
        if (
                e.getClickedInventory() == i ||
                        a == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                        a == InventoryAction.COLLECT_TO_CURSOR) noPerm(e, p);
    }
}
