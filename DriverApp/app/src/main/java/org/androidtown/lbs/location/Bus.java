package org.androidtown.lbs.location;

public class Bus {
    private int lastStaion;
    private int time;
    private int currentStation;


    public Bus() {
        lastStaion = 0;
        currentStation = 0;
        time = 0;
    }

    public int getLastStaion() {
        return lastStaion;
    }

    public int getTime() {
        return time;
    }

    public int getCurrentStation() {
        return currentStation;
    }

    public void setLastStation(int station) {
        lastStaion = station;
    }

    public void setTime(int takeTime) {
        time = takeTime;
    }

    public void setCurrentStation(int station) {
        currentStation = station;
    }

}
