package com.monowii.quakecraft.Models.enums.upgrades;

public enum CooldownUpgrade {
    DEFAULT(1.5D),
    s1_3(1.3D),
    s1_4(1.4D),
    s1_2(1.2D),
    s1_1(1.1D),
    s1_0(1.0D),
    s9_0(9.0D),
    s0_9(0.9D);

    private double value;

    private CooldownUpgrade(double value) {
        this.value = value;
    }

    public static double getValueById(int id) {
        return values()[id].value;
    }
}
