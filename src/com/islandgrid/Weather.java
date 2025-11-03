package com.islandgrid;

import java.util.Random;

public class Weather {
    public enum Condition {
        SUNNY, WINDY, RAINY, CLOUDY
    }

    private Condition current;
    private long lastChangeTime;
    private static final long CHANGE_INTERVAL = 10_000; // every 10 s
    private Random random;

    public Weather() {
        random = new Random();
        current = Condition.SUNNY;
        lastChangeTime = System.currentTimeMillis();
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastChangeTime > CHANGE_INTERVAL) {
            Condition[] values = Condition.values();
            current = values[random.nextInt(values.length)];
            lastChangeTime = now;
        }
    }

    public Condition getCurrent() {
        return current;
    }

    /** multiplier that affects energy generation */
    public double getMultiplier(String type) {
        switch (current) {
            case SUNNY:
                if (type.equalsIgnoreCase("solar")) return 1.5;
                if (type.equalsIgnoreCase("wind")) return 1.0;
                if (type.equalsIgnoreCase("hydro")) return 0.9;
                break;
            case WINDY:
                if (type.equalsIgnoreCase("wind")) return 1.5;
                if (type.equalsIgnoreCase("hydro")) return 1.1;
                break;
            case RAINY:
                if (type.equalsIgnoreCase("hydro")) return 1.6;
                if (type.equalsIgnoreCase("solar")) return 0.7;
                break;
            case CLOUDY:
                if (type.equalsIgnoreCase("solar")) return 0.6;
                break;
        }
        return 1.0;
    }
}
