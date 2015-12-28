package com.monowii.quakecraft;

import java.util.*;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monowii.quakecraft.Models.QCPlayer;
import com.monowii.quakecraft.Models.enums.GameStatus;
import com.monowii.quakecraft.Utils.Utils;

import com.monowii.quakecraft.stats.GameStat;
import com.monowii.quakecraft.stats.PlayerStat;
import com.updg.CR_API.MQ.senderStatsToCenter;
import com.updg.CR_API.MQ.senderUpdatesToCenter;
import com.updg.CR_API.Utils.L;
import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;


public class QuakeCraft implements CommandExecutor {
    public int minPlayers = 10;
    public int killLimit = 20;
    public int vipPlayersFrom = 10;
    public int maxPlayers = 11;
    public int mapId = 0;

    public int tillGame = 60;
    public int tillGameDefault = 60;
    public int tillGameShedule = 0;

    public long timeStart = 0;
    public long timeEnd = 0;

    public static String pluginPrefix = "[" + ChatColor.GOLD + "QuakeCraft" + ChatColor.WHITE + "] " + ChatColor.GOLD;

    public Objective objective = null;
    public Scoreboard board = null;

    public HashMap<String, QCPlayer> players = new HashMap<String, QCPlayer>();
    public ArrayList<Location> spawns = new ArrayList<Location>();

    private GameStatus status = GameStatus.RELOAD;

    public QCPlayer winner = null;

    public QuakeCraft(int minPlayers, int vipPlayersFrom, int maxPlayers, int killLimit, int mapId, List<Location> spawns) {
        this.minPlayers = minPlayers;
        this.killLimit = killLimit;
        this.vipPlayersFrom = vipPlayersFrom;
        this.maxPlayers = maxPlayers;
        this.mapId = mapId;
        this.spawns.addAll(spawns);
    }

    public void send() {
        String s = GameStatus.WAITING.toString();
        if (Plugin.game.maxPlayers <= Plugin.game.getActivePlayers())
            s = "IN_GAME";
        if (Plugin.game.getStatus() == GameStatus.WAITING) {
            if (Plugin.game.tillGame < Plugin.game.tillGameDefault)
                senderUpdatesToCenter.send(Plugin.plugin.serverId + ":" + s + ":" + "В ОЖИДАНИИ" + ":" + Plugin.game.getActivePlayers() + ":" + Plugin.game.maxPlayers + ":До игры " + Plugin.game.tillGame + " c.");
            else
                senderUpdatesToCenter.send(Plugin.plugin.serverId + ":" + s + ":" + "В ОЖИДАНИИ" + ":" + Plugin.game.getActivePlayers() + ":" + Plugin.game.maxPlayers + ":Набор игроков");
        } else if (Plugin.game.getStatus() == GameStatus.POSTGAME) {
            senderUpdatesToCenter.send(Plugin.plugin.serverId + ":IN_GAME:" + "ИГРА ОКОНЧЕНА" + ":" + Plugin.game.getActivePlayers() + ":" + Plugin.game.maxPlayers + ":Победил " + Plugin.game.winner.getName());
        } else if (Plugin.game.getStatus() == GameStatus.INGAME || Plugin.game.getStatus() == GameStatus.POSTGAME)
            senderUpdatesToCenter.send(Plugin.plugin.serverId + ":IN_GAME:" + "ИГРА" + ":" + Plugin.game.getActivePlayers() + ":" + Plugin.game.maxPlayers + ":БОЙ");
        else if (Plugin.game.getStatus() == GameStatus.RELOAD)
            senderUpdatesToCenter.send(Plugin.plugin.serverId + ":DISABLED:" + "ОФФЛАЙН" + ":0:0:");

    }

    public void getReady() {
        setStatus(GameStatus.WAITING);
        new TopBarThread().start();
        send();
    }

    public int getActivePlayers() {
        int activePlayers = 0;

        for (QCPlayer player : players.values()) {
            if (!player.getStat().isExit()) {
                activePlayers++;
            }
        }

        return activePlayers;
    }

    public Collection<QCPlayer> getActivePlayersArray() {
        Collection<QCPlayer> o = new ArrayList<QCPlayer>();
        for (QCPlayer player : players.values()) {
            if (!player.isSpectator()) {
                o.add(player);
            }
        }
        return o;
    }

    public Collection<QCPlayer> getSpectatorsArray() {
        Collection<QCPlayer> o = new ArrayList<QCPlayer>();
        for (QCPlayer player : players.values()) {
            if (player.isSpectator()) {
                o.add(player);
            }
        }
        return o;
    }

    class TimeAndWeather extends Thread {

        private World world;

        public TimeAndWeather(World world) {
            this.world = world;
        }

        @Override
        public void run() {
            world.setTime(6500);
        }


    }

    public boolean isAvailableToStart() {
        return this.getActivePlayers() >= this.minPlayers;
    }

    public void refreshTimer() {
        if (this.status != GameStatus.INGAME && this.status != GameStatus.RELOAD) {
            if (isAvailableToStart() && this.tillGame == this.tillGameDefault && this.tillGameShedule == 0) {
                this.tillGameShedule = Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.plugin, new Runnable() {
                    public void run() {
                        updateTimer();
                    }
                }, 0, 20);
            }
        }
    }

    private void updateTimer() {
        if (isAvailableToStart()) {
            this.tillGame--;
            if (this.tillGame == 0) {
                Bukkit.getScheduler().cancelTask(this.tillGameShedule);
                for (QCPlayer player : this.players.values()) {
                    if (!player.getStat().isExit())
                        player.playSound("random.levelup");
                }
                this.startGame();
            }
            if (this.tillGame <= this.tillGameDefault && this.tillGame > 0) {
                for (QCPlayer player : this.players.values()) {
                    if (!player.getStat().isExit())
                        player.playSound("random.orb");
                }
            }
            send();
        } else {
            Bukkit.getScheduler().cancelTask(this.tillGameShedule);
            this.tillGameShedule = 0;
            this.tillGame = this.tillGameDefault;
            this.status = GameStatus.WAITING;
            send();
            Bukkit.broadcastMessage(pluginPrefix + "Отмена старта. Недостаточно игроков.");
        }
    }

    class ReloadThread extends Thread {

        private QCPlayer player;

        public ReloadThread(QCPlayer player) {
            this.player = player;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            if (player.isReloading()) {
                float time;

                if ((time = (System.currentTimeMillis() - player.getShotTime())) < (player.getWeapon().getCooldown() * 1000)) {
                    if (time != 0) {
                        player.getBukkitPlayer().setExp(1.0F - 1.0F / ((float) (player.getWeapon().getCooldown() * 1000) / time));
                    }
                } else {
                    player.getBukkitPlayer().setExp(0.0F);
                    player.setReloading(false);
                }
            }
        }
    }

    public class TopBarThread extends Thread {
        public void run() {
            while (true) {
                try {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (getStatus() == GameStatus.WAITING) {
                            if (tillGame != tillGameDefault)
                                BarAPI.setMessage(p, ChatColor.GREEN + "До игры" + Utils.plural(tillGame, " осталась " + tillGame + " секунда", " осталось " + tillGame + " секунды", " осталось " + tillGame + " секунд") + ".", (float) tillGame / ((float) tillGameDefault / 100F));
                            else if (getActivePlayers() < minPlayers)
                                BarAPI.setMessage(p, ChatColor.GREEN + "Ожидаем игроков. " + ChatColor.YELLOW + "(" + getActivePlayers() + "/" + minPlayers + "/" + maxPlayers + ")", (float) getActivePlayers() / ((float) minPlayers / 100F));
                            else
                                BarAPI.setMessage(p, ChatColor.GREEN + "Ожидаем игроков.", 100F);
                        }
                        if (getStatus() == GameStatus.INGAME) {
                            BarAPI.setMessage(p, ChatColor.GREEN + "Бой", 100F);
                        }
                        if (getStatus() == GameStatus.POSTGAME) {
                            BarAPI.setMessage(p, ChatColor.AQUA + "Победил " + winner.getName(), 100F);
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void endGame(QCPlayer winner) {
        setStatus(GameStatus.POSTGAME);
        this.winner = winner;

        timeEnd = System.currentTimeMillis() / 1000;

        for (QCPlayer player : this.players.values()) {
            if (!player.getStat().isExit()) {
                player.getStat().setTimeInGame(System.currentTimeMillis() / 1000L - this.timeStart);
                player.getBukkitPlayer().getInventory().clear();
            }
        }

        Bukkit.broadcastMessage(pluginPrefix + "Победил игрок: " + winner.getName());
        Bukkit.broadcastMessage(pluginPrefix + "Сервер перезагрузиться через 15 секунд");

        send();

        new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            Bukkit.broadcastMessage(pluginPrefix + "Сервер перезагрузится через 10 секунд.");
                            Thread.sleep(5000);
                            Bukkit.broadcastMessage(pluginPrefix + "Сервер перезагрузится через 5 секунд.");
                            Thread.sleep(5000);
                            Bukkit.broadcastMessage(pluginPrefix + "Сервер перезагружается.");
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        sendStats();
                        status = GameStatus.RELOAD;
                        send();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    }
                }).start();
    }

    public void sendStats() {
        GameStat game = new GameStat();
        game.setServerId(Plugin.plugin.serverId);
        game.setMapId(mapId);
        game.setWinner(winner.getId());
        game.setStart(timeStart);
        game.setEnd(timeEnd);
        List<PlayerStat> players = new ArrayList<PlayerStat>();
        PlayerStat tmpPlayer;
        for (QCPlayer p : getActivePlayersArray()) {
            tmpPlayer = new PlayerStat();
            tmpPlayer.setPlayerId(p.getId());
            tmpPlayer.setTimeInGame(p.getStat().getTimeInGame());
            tmpPlayer.setShots(p.getStat().getShots());
            tmpPlayer.setKilled(p.getStat().getKilled());
            tmpPlayer.setDeaths(p.getStat().getDeaths());
            tmpPlayer.setExit(p.getStat().isExit());
            players.add(tmpPlayer);
        }
        game.setPlayers(players);
        try {
            String stat = new ObjectMapper().writeValueAsString(game);
            senderStatsToCenter.send("quakecraft", stat);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    public void addPlayer(QCPlayer p) {
        this.players.put(p.getName().toLowerCase(), p);
    }

    public void removePlayer(QCPlayer p) {
        if (this.players.containsKey(p.getName().toLowerCase()))
            this.players.remove(p.getName().toLowerCase());
    }

    public void removePlayer(String player) {
        if (this.players.containsKey(player.toLowerCase()))
            this.players.remove(player.toLowerCase());
    }

    public void startGame() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            QCPlayer player = getQCPlayer(p.getName());

            p.teleport(Utils.getRandomLocation());

            p.getInventory().clear();
            p.getInventory().addItem(player.getWeapon().getBase());

            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));

            p.setScoreboard(board);

            Score s1 = objective.getScore(p);
            s1.setScore(1);
            updateScoreboard();
            s1.setScore(0);
            updateScoreboard();

            Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.plugin, new ReloadThread(player), 0, 2L);
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.plugin, new TimeAndWeather(Bukkit.getWorlds().get(0)), 1000L, 1000L);
        timeStart = System.currentTimeMillis() / 1000;
        setStatus(GameStatus.INGAME);
        Bukkit.broadcastMessage(pluginPrefix + "Да прибудет с вами сила!");
    }

    public QCPlayer getQCPlayer(String player) {
        QCPlayer qcp;
        if ((qcp = this.players.get(player.toLowerCase())) != null) {
            return qcp;
        }

        return null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
        if (!sender.hasPermission("qc.admin")) {
            sender.sendMessage(ChatColor.RED + "Недостаточно прав");
            return true;
        }
        Player p;
        if (sender instanceof Player) {
            p = (Player) sender;
        } else {
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("startgame") && p.isOp()) {
            this.startGame();
        }
        return false;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public void updateScoreboard() {
        for (QCPlayer online : this.players.values()) {
            if (!online.getStat().isExit())
                online.getBukkitPlayer().setScoreboard(board);
        }
    }

}
