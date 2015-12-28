package com.monowii.quakecraft.Listeners;

import com.monowii.quakecraft.Models.QCPlayer;
import com.monowii.quakecraft.Models.enums.GameStatus;
import com.monowii.quakecraft.Plugin;
import com.monowii.quakecraft.Utils.EconomicSettings;
import com.monowii.quakecraft.Utils.Utils;
import com.updg.CR_API.DataServer.DSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

/**
 * @author Sceri
 */
public class GameListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (Plugin.game.getStatus() == GameStatus.INGAME) {
            Player p = e.getPlayer();
            QCPlayer qcp = Plugin.game.getQCPlayer(p.getName());

            if (e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == qcp.getWeapon().getBase().getType() && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                if (!qcp.isReloading()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.plugin, new FireworkPath(qcp));
                    Bukkit.getServer().getWorld("world").playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 1, 1);
                }
            }
        }
    }

    class FireworkPath extends Thread {

        private QCPlayer player;

        public FireworkPath(QCPlayer player) {
            this.player = player;
        }

        @Override
        public void run() {
            Player p = player.getBukkitPlayer();

            double yaw = Math.toRadians(-p.getLocation().getYaw() - 90.0F);
            double pitch = Math.toRadians(-p.getLocation().getPitch());
            double x = Math.cos(pitch) * Math.cos(yaw);
            double y = Math.sin(pitch);
            double z = -Math.sin(yaw) * Math.cos(pitch);
            Vector dirVel = new Vector(x, y, z);

            BlockIterator lineOfSight = new BlockIterator(p.getWorld(), p.getEyeLocation().toVector(), dirVel.normalize(), 0.0D, player.getWeapon().getRange());

            while (lineOfSight.hasNext()) {
                Block block = lineOfSight.next();
                if (block.getType() != Material.AIR) {
                    break;
                }

                for (QCPlayer qcPlayer : Plugin.game.players.values()) {
                    if (!qcPlayer.getStat().isExit())
                        qcPlayer.playEffect("fireworksSpark", block.getLocation(), 0F, 0F, 0F, 0, 1);
                }

                Player e;
                if ((e = Utils.getNearbyPlayer(block.getLocation(), player.getWeapon())) != null) {
                    if (e != player.getBukkitPlayer()) {
                        if (!e.isDead()) {
                            Firework fw = Utils.buildFirework(e.getLocation(), player.getWeapon());
                            fw.detonate();
                            player.addFrag(Plugin.game.getQCPlayer(e.getName()));

                            if (player.getStat().getKilled().size() >= Plugin.game.killLimit) {
                                DSUtils.addPlayerExpAndMoney(player.getBukkitPlayer(), EconomicSettings.win, 0);
                                Plugin.game.endGame(player);
                            }

                            break;
                        }
                    }
                }

            }

            player.getStat().addShot();
            player.setShotTime(System.currentTimeMillis());
            player.setReloading(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }
}