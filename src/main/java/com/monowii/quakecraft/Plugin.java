package com.monowii.quakecraft;

import com.monowii.quakecraft.Listeners.GameListener;
import com.monowii.quakecraft.Listeners.PlayerListener;
import com.monowii.quakecraft.Listeners.SystemListener;
import com.monowii.quakecraft.Models.enums.GameStatus;
import com.monowii.quakecraft.Utils.Utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Alex
 * Date: 13.11.13  5:13
 */
public class Plugin extends JavaPlugin {
    public static Plugin plugin;
    public static QuakeCraft game;
    public int serverId = 0;

    @Override
    public void onEnable() {
        plugin = this;

        this.saveDefaultConfig();

        this.getServer().getPluginManager().registerEvents(new GameListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new SystemListener(), this);


        ArrayList<Location> spawnsCache = new ArrayList<Location>();

        for (String item : getConfig().getStringList("spawns")) {
            spawnsCache.add(Utils.stringToLoc(item));
        }

        serverId = getConfig().getInt("serverId");

        game = new QuakeCraft(getConfig().getInt("minPlayers", 10), getConfig().getInt("vipPlayersFrom", 10), getConfig().getInt("maxPlayers", 11), getConfig().getInt("killLimit", 20), getConfig().getInt("mapId", 0), spawnsCache);
        Utils.resetScoreboard();

        World world = Bukkit.getWorlds().get(0);
        world.setThundering(false);
        world.setStorm(false);
        world.setWeatherDuration(1000000);
        world.setTime(0);

        getCommand("startgame").setExecutor(game);

        game.getReady();
    }

    public void onDisable() {
        game.setStatus(GameStatus.RELOAD);
        Plugin.game.send();
    }
}
