package com.monowii.quakecraft.Models.enums.upgrades;

public enum SpreadUpgrade {
    DEFAULT(2D);
    
    private double value;
    
    private SpreadUpgrade(double value) {
        this.value = value;
    }
    
    public static double getValueById(int id) {
        return values()[id].value;
    }
    
}
