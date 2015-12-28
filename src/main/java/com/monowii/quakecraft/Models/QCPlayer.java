package com.monowii.quakecraft.Models;

import com.monowii.quakecraft.Plugin;
import com.monowii.quakecraft.QuakeCraft;

import java.util.HashMap;
import java.util.logging.Level;

import com.monowii.quakecraft.Utils.EconomicSettings;
import com.updg.CR_API.Bungee.Bungee;
import com.updg.CR_API.DataServer.DSUtils;
import com.updg.CR_API.Utils.L;
import net.minecraft.server.v1_7_R2.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_7_R2.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * User: Alex
 * Date: 14.06.13
 * Time: 18:24
 */
public class QCPlayer {
    private Player bukkitPlayer;
    private QCWeapon weapon;
    private String name;
    private QCPlayerStat stat;

    private int id;
    private int score = 0;

    private long shotTime = 0;

    private boolean reload = false;
    private int rang;
    private int vip;

    public QCPlayer(Player player) {
        L.$(Level.INFO, "New player obj:" + player.getName());
        this.bukkitPlayer = player;
        this.name = player.getName();
        this.stat = new QCPlayerStat();
        String[] args = DSUtils.QC_getParams(this.getBukkitPlayer());
        this.weapon = args == null ? new QCWeapon(new String[]{"0", "0", "0", "0", "0", "0"}) : new QCWeapon(args);
    }

    public void playEffect(String particleName, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        try {
            Player player = this.getBukkitPlayer();
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleName, (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, count);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addFrag(QCPlayer victim) {
        DSUtils.addPlayerExpAndMoney(this.getBukkitPlayer(), EconomicSettings.kill, 0);

        stat.addKill(System.currentTimeMillis() / 1000, victim.getId());
        victim.addDeaths();
        victim.getBukkitPlayer().setHealth(0);

        Plugin.game.objective.getScore(getBukkitPlayer()).setScore(plusScore());
        Plugin.game.updateScoreboard();

        Bukkit.broadcastMessage(QuakeCraft.pluginPrefix + "Игрок " + getName() + " убил " + victim.getName());

        for (QCPlayer online : Plugin.game.players.values()) {
            if (!online.getStat().isExit())
                online.getBukkitPlayer().playSound(online.getBukkitPlayer().getLocation(), Sound.GHAST_DEATH, 1, 1);
        }
    }

    public QCPlayerStat getStat() {
        return this.stat;
    }

    public void forceToLobby() {
        Bungee.teleportPlayer(getBukkitPlayer(), "lobby");
    }

    public void addExp(int exp) {
        DSUtils.addPlayerExpAndMoney(this.getBukkitPlayer(), exp, 0);
    }

    public void addDeaths() {
        this.stat.addDeath();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShotTime(long time) {
        this.shotTime = time;
    }

    public int getScore() {
        return score;
    }

    public boolean isReloading() {
        return this.reload;
    }

    public void setReloading(boolean reload) {
        this.reload = reload;
    }

    public long getShotTime() {
        return this.shotTime;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Player getBukkitPlayer() {
        return this.bukkitPlayer;
    }

    public QCWeapon getWeapon() {
        return this.weapon;
    }

    public void setWeapon(QCWeapon weapon) {
        this.weapon = weapon;
    }

    public int plusScore() {
        return ++this.score;
    }

    public void playSound(String sound) {
        Player p = this.getBukkitPlayer();
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(sound, p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ(), 1.0F, 1.0F);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public void setRang(int rang) {
        this.rang = rang;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public boolean isSpectator() {
        //TODO
        return false;
    }
}
