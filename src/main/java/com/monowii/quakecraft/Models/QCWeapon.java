package com.monowii.quakecraft.Models;

import com.monowii.quakecraft.Models.enums.upgrades.ColorUpgrade;
import com.monowii.quakecraft.Models.enums.upgrades.CooldownUpgrade;
import com.monowii.quakecraft.Models.enums.upgrades.FWTypeUpgrade;
import com.monowii.quakecraft.Models.enums.upgrades.RailUpgrade;
import com.monowii.quakecraft.Models.enums.upgrades.RangeUpgrade;
import com.monowii.quakecraft.Models.enums.upgrades.SpreadUpgrade;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.ItemStack;

public class QCWeapon {

    private ItemStack base;
    private int range;
    private double spread;
    private Color fireworkColor;
    private FireworkEffect.Type type;
    private double cooldownSec;

    public QCWeapon(String[] upgrades) {
        this.base = RailUpgrade.getValueById(Integer.parseInt(upgrades[0]));
        this.range = RangeUpgrade.getValueById(Integer.parseInt(upgrades[1]));
        this.spread = SpreadUpgrade.getValueById(Integer.parseInt(upgrades[2]));
        this.fireworkColor = ColorUpgrade.getValueById(Integer.parseInt(upgrades[3]));
        this.type = FWTypeUpgrade.getValueById(Integer.parseInt(upgrades[4]));
        this.cooldownSec = CooldownUpgrade.getValueById(Integer.parseInt(upgrades[5]));
    }

    public ItemStack getBase() {
        return this.base;
    }

    public void setBase(ItemStack base) {
        this.base = base;
    }

    public int getRange() {
        return this.range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public double getSpread() {
        return this.spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }
    
    public void setCooldown(double sec) {
        this.cooldownSec = sec;
    }
    
    public double getCooldown() {
        return this.cooldownSec;
    }

    public Color getFireworkColor() {
        return this.fireworkColor;
    }

    public void setFireworkColor(Color color) {
        this.fireworkColor = color;
    }

    public FireworkEffect.Type getEffectType() {
        return this.type;
    }

    public void setEffectType(FireworkEffect.Type type) {
        this.type = type;
    }
}
