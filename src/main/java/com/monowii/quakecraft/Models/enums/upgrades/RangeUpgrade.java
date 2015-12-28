package com.monowii.quakecraft.Models.enums.upgrades;

public enum RangeUpgrade {

    DEFAULT(30);
    
    private int value;

    private RangeUpgrade(int value) {
        this.value = value;
    }

    public static int getValueById(int id) {
        return values()[id].value;
    }
}
