package com.monowii.quakecraft.Models.enums.upgrades;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum RailUpgrade {

    DEFAULT(RailUpgrade.getDefault());
    
    private ItemStack value;

    private RailUpgrade(ItemStack value) {
        this.value = value;
    }

    public static ItemStack getValueById(int id) {
        return values()[id].value;
    }
    
    public static ItemStack getDefault() {
        ItemStack item = new ItemStack(Material.WOOD_HOE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("MEGA-KILLER-9000");
        item.setItemMeta(meta);
        return item;
    }
}
