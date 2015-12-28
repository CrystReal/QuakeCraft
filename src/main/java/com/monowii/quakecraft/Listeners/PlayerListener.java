package com.monowii.quakecraft.Listeners;

import com.monowii.quakecraft.Models.QCPlayer;
import com.monowii.quakecraft.Models.enums.GameStatus;
import com.monowii.quakecraft.Plugin;
import com.monowii.quakecraft.QuakeCraft;
import com.monowii.quakecraft.Utils.EconomicSettings;
import com.monowii.quakecraft.Utils.Utils;
import com.updg.CR_API.APIPlugin;
import com.updg.CR_API.DataServer.DSUtils;
import com.updg.CR_API.Models.APIPlayer;
import com.updg.CR_API.Utils.L;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.logging.Level;

/**
 * @author Sceri
 */
public class PlayerListener implements Listener {

    int taskId;
    int count = 10;

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player user = event.getPlayer();

        QCPlayer p = Plugin.game.getQCPlayer(user.getName());
        if (p == null) {
            p = new QCPlayer(user);
            if (Plugin.game.getStatus() == GameStatus.WAITING) {
                if (Plugin.game.getActivePlayers() < Plugin.game.maxPlayers)
                    Plugin.game.addPlayer(p);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        e.setJoinMessage(null);
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        p.getInventory().clear();
        p.teleport(Utils.getRandomLocation());
        p.setGameMode(GameMode.ADVENTURE);

        final QCPlayer qcp = Plugin.game.getQCPlayer(e.getPlayer().getName());
        Plugin.game.send();

        Plugin.game.refreshTimer();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        Player p = e.getPlayer();

        final QuakeCraft game = Plugin.game;

        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }

        if (game.getStatus() == GameStatus.INGAME) {
            QCPlayer looser = game.getQCPlayer(p.getName());
            looser.getStat().setExit(true);
            looser.getStat().setTimeInGame(System.currentTimeMillis() / 1000 - game.timeStart);

            if (game.getActivePlayers() < 2) {
                for (QCPlayer player : game.players.values()) {
                    if (!player.getStat().isExit()) {
                        Bukkit.broadcastMessage(QuakeCraft.pluginPrefix + "Игра окончена! Игрок " + player.getName() + " одержал техническую победу!");
                        DSUtils.addPlayerExpAndMoney(player.getBukkitPlayer(), EconomicSettings.technicalWin, 0);
                        game.endGame(player);
                    }
                }
            }
        } else if (game.getStatus() != GameStatus.POSTGAME && game.getStatus() != GameStatus.RELOAD) {
            game.removePlayer(p.getName());
        }

        game.refreshTimer();
        Plugin.game.send();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();

            if (!p.isDead()) {
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().clear();
        e.setDeathMessage("");
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        final Player p = e.getPlayer();

        p.getInventory().clear();

        if (Plugin.game.getStatus() == GameStatus.INGAME) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.plugin, new Runnable() {
                public void run() {
                    p.getInventory().addItem(Plugin.game.getQCPlayer(p.getName()).getWeapon().getBase());
                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, true));
                }
            }, 2);
        }

        e.setRespawnLocation(Utils.getRandomLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if ((event.getPlayer().getGameMode() != GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        e.setCancelled(true);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Plugin.game.getStatus() == GameStatus.INGAME) {
            Player p = event.getPlayer();
            Block up = p.getLocation().getBlock();
            if (up.getType() == Material.STONE_PLATE || up.getRelative(BlockFace.DOWN).getType() == Material.STONE_PLATE) {
                p.setVelocity(p.getLocation().getDirection().multiply(1.2));
                p.setVelocity(new Vector(p.getVelocity().getX(), 1.3D, p.getVelocity().getZ()));
            }
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e) {
        if (e.getBlock().getTypeId() == 39) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChangeHunger(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e) {
        QCPlayer p = Plugin.game.getQCPlayer(e.getPlayer().getName().toLowerCase());
        APIPlayer apiPlayer = APIPlugin.getPlayer(e.getPlayer().getName());
        String msg = apiPlayer.getPrefix() + ChatColor.RESET + apiPlayer.getNickColor() + e.getPlayer().getDisplayName() + ChatColor.RESET + apiPlayer.getColonColor() + ": " + ChatColor.RESET + apiPlayer.getMessageColor() + e.getMessage();
        if (Plugin.game.getStatus() == GameStatus.INGAME) {
            if (p.isSpectator()) {
                for (QCPlayer i : Plugin.game.getSpectatorsArray()) {
                    i.getBukkitPlayer().sendMessage(msg);
                }
            } else {
                for (QCPlayer i : Plugin.game.getActivePlayersArray()) {
                    i.getBukkitPlayer().sendMessage(msg);
                }
                for (QCPlayer i : Plugin.game.getSpectatorsArray()) {
                    i.getBukkitPlayer().sendMessage(msg);
                }
            }
        } else {
            for (QCPlayer i : Plugin.game.getActivePlayersArray()) {
                i.getBukkitPlayer().sendMessage(msg);
            }
            for (QCPlayer i : Plugin.game.getSpectatorsArray()) {
                i.getBukkitPlayer().sendMessage(msg);
            }
        }
        e.setCancelled(true);
    }
}
