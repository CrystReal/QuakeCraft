package com.monowii.quakecraft.Models;

import java.util.HashMap;

/**
 * Created by Sceri on 08.01.14.
 */
public class QCPlayerStat {

    private HashMap<Long, Integer> killed = new HashMap<Long, Integer>();

    private long _timeInGame = 0;
    private int _shots = 0;
    private int _deaths = 0;
    private boolean _exit = false;

    public long getTimeInGame() {
        return _timeInGame;
    }

    public void setTimeInGame(long _timeInGame) {
        this._timeInGame = _timeInGame;
    }

    public int getShots() {
        return _shots;
    }

    public void addShot() {
        this._shots++;
    }

    public void addKill(Long time, int id) {
        this.killed.put(time, id);
    }

    public HashMap<Long, Integer> getKilled() {
        return this.killed;
    }

    public void addDeath() {
        this._deaths++;
    }

    public int getDeaths() {
        return this._deaths;
    }

    public void setExit(boolean exit) {
        this._exit = exit;
    }

    public boolean isExit() {
        return this._exit;
    }

}
