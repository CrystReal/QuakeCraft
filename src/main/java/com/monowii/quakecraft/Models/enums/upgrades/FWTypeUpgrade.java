package com.monowii.quakecraft.Models.enums.upgrades;

import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;

public enum FWTypeUpgrade {
    DEFAULT(Type.BALL);
    
    private Type value;
    
    private FWTypeUpgrade(FireworkEffect.Type value) {
        this.value = value;
    }
    
    public static Type getValueById(int id) {
        return values()[id].value;
    }
    
}
