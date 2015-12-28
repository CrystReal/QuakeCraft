package com.monowii.quakecraft.Utils;

import com.monowii.quakecraft.Models.QCPlayer;
import com.monowii.quakecraft.Models.QCWeapon;
import com.monowii.quakecraft.Plugin;
import com.monowii.quakecraft.QuakeCraft;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 *
 * @author Sceri
 */
public class Utils {

    public static void resetScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        QuakeCraft game = Plugin.game;

        game.board = manager.getNewScoreboard();
        game.objective = game.board.registerNewObjective("kills", "dummy");
        game.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        game.objective.setDisplayName("Счет");
    }

    public static Location stringToLoc(String string) {
        String[] loc = string.split("\\|");
        World world = Bukkit.getWorld(loc[0]);
        Double x = Double.parseDouble(loc[1]);
        Double y = Double.parseDouble(loc[2]);
        Double z = Double.parseDouble(loc[3]);

        return new Location(world, x, y, z);
    }

    public static Location getRandomLocation() {
        Random randomGenerator = new Random();
        Location redSpawn = Plugin.game.spawns.get(randomGenerator.nextInt(Plugin.game.spawns.size()));
        redSpawn.setPitch(12);
        redSpawn.setYaw(-90);

        return redSpawn;
    }

    public static Firework buildFirework(Location location, QCWeapon weapon) {
        Firework fw = (Firework) location.getWorld().spawn(location, Firework.class);
        FireworkMeta fm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().withColor(weapon.getFireworkColor()).with(weapon.getEffectType()).build();
        fm.addEffect(effect);
        fm.setPower(1);
        fw.setFireworkMeta(fm);
        return fw;
    }

    public static Player getNearbyPlayer(Location loc, QCWeapon weapon) {
        for (QCPlayer player : Plugin.game.players.values()) {
            if (!player.getStat().isExit()) {
                double distance = player.getBukkitPlayer().getLocation().distance(loc);
                if (distance < weapon.getSpread()) {
                    return player.getBukkitPlayer();
                }
            }
        }
        return null;
    }

    /**
     * This method return plural form of word based on int value.
     *
     * @param number Int value
     * @param form1  First form of word (i == 1)
     * @param form2  Second form of word (i > 1 && i < 5)
     * @param form3  Third form of word (i > 10 && i < 20)
     * @return string
     */
    public static String plural(int number, String form1, String form2, String form3) {
        int n1 = Math.abs(number) % 100;
        int n2 = number % 10;
        if (n1 > 10 && n1 < 20) return form3;
        if (n2 > 1 && n2 < 5) return form2;
        if (n2 == 1) return form1;
        return form3;
    }
}
