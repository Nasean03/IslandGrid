package com.islandgrid;

import com.islandgrid.weather.OpenMeteoClient;

import java.time.ZoneId;
import java.util.Random;

public class Weather {

    public enum Condition {
        SUNNY, WINDY, RAINY, CLOUDY
    }

    // ----- existing random-weather stuff (fallback) -----
    private Condition current;
    private long lastChangeTime;
    private static final long CHANGE_INTERVAL = 10_000; // every 10 s
    private final Random random;

    // ----- real weather stuff -----
    private final OpenMeteoClient meteoClient = new OpenMeteoClient();
    private OpenMeteoClient.WeatherSample liveSample;
    private long lastApiFetchTime = 0;

    // fetch every 30 minutes (Open-Meteo updates hourly; 30m is fine)
    private static final long API_FETCH_INTERVAL = 30L * 60L * 1000L;

    private final String apiUrl;
    private final ZoneId zone;

    // Turn this on/off easily
    private boolean useLiveData = true;

    public Weather(String apiUrl, ZoneId zone) {
        this.apiUrl = apiUrl;
        this.zone = zone;

        random = new Random();
        current = Condition.SUNNY;
        lastChangeTime = System.currentTimeMillis();
    }

    // If you still need an old constructor somewhere:
    public Weather() {
        this(
            "https://api.open-meteo.com/v1/forecast?latitude=13.1939&longitude=-59.5432&hourly=temperature_2m,shortwave_radiation,direct_normal_irradiance,diffuse_radiation,wind_speed_120m,wind_speed_80m,wind_speed_10m,wind_gusts_10m,precipitation,is_day&timezone=auto",
            ZoneId.of("America/Barbados")

        );
        useLiveData = false; // default off for the no-arg version
    }

    public void setUseLiveData(boolean useLiveData) {
        this.useLiveData = useLiveData;
    }

    public boolean isUsingLiveData() {
        return useLiveData;
    }

    public void update() {
        long now = System.currentTimeMillis();

        // 1) keep your random condition system (fallback)
        if (!useLiveData && now - lastChangeTime > CHANGE_INTERVAL) {
            Condition[] values = Condition.values();
            current = values[random.nextInt(values.length)];
            lastChangeTime = now;
        }

        // 2) occasionally refresh live weather
        if (useLiveData && (now - lastApiFetchTime > API_FETCH_INTERVAL)) {
            lastApiFetchTime = now;

            meteoClient.fetchNow(apiUrl, zone)
                    .thenAccept(sample -> {
                        liveSample = sample;
                        // update the enum condition based on real data too (optional)
                        current = inferConditionFromLive(sample);
                    })
                    .exceptionally(ex -> {
                        // If API fails, keep using random weather silently
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    public Condition getCurrent() {
        return current;
    }

    // Optional: expose raw sample for UI/debug
    public OpenMeteoClient.WeatherSample getLiveSample() {
        return liveSample;
    }

    /** multiplier that affects energy generation */
    public double getMultiplier(String type) {
        // If we have live data, use it
        if (useLiveData && liveSample != null) {
            return getLiveMultiplier(type, liveSample);
        }

        // Otherwise use your existing random rules
        return getRandomMultiplier(type);
    }

    // ----- live multiplier logic -----

    private double getLiveMultiplier(String type, OpenMeteoClient.WeatherSample s) {
        String t = type.toLowerCase();

        if (t.equals("solar")) {
            // shortwave_radiation ~ 0..1000+ W/m² typical
            double base = clamp(s.shortwaveRadiation() / 1000.0);
            if (s.isDay() == 0) base = 0;
            // Turn 0..1 into a game-friendly multiplier, e.g. 0.3..1.6
            return 0.3 + (1.3 * base);
        }

        if (t.equals("wind")) {
            // pick 120m wind for turbines (km/h if you kept default)
            double base = clamp(s.windSpeed120m() / 50.0);
            // gust storms can reduce output (turbine cut-out)
            if (s.windGusts10m() > 80) base *= 0.2;
            return 0.5 + (1.2 * base); // 0.5..1.7-ish
        }

        if (t.equals("hydro")) {
            // precipitation mm/hr -> more rain = higher hydro inflow
            double rain = Math.max(0, s.precipitation());
            // map 0..10mm/hr to 0.8..1.6
            double base = clamp(rain / 10.0);
            return 0.8 + (0.8 * base);
        }

        return 1.0;
    }

    private Condition inferConditionFromLive(OpenMeteoClient.WeatherSample s) {
        // Simple heuristics:
        if (s.precipitation() >= 0.5) return Condition.RAINY;
        if (s.shortwaveRadiation() >= 600 && s.isDay() == 1) return Condition.SUNNY;
        if (s.windSpeed120m() >= 25) return Condition.WINDY; // km/h threshold; tweak
        return Condition.CLOUDY;
    }

    // ----- your original random multiplier logic -----

    private double getRandomMultiplier(String type) {
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

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
