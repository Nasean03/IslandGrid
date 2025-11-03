package com.islandgrid;

//import javafx.scene.paint.Color;

public class EnergyManager {
    private int energySupply;
    private int energyDemand;
    private int pollutionLevel;
    private int batteryLevel;
    private int batteryCapacity; 
    private boolean powerCrisis;
    private boolean overcharged;
    private Grid grid;
    private GameView gameView;
    private boolean pollutionLevelHigh;
    private Weather weather;


    private int demandCounter = 0;    
    private boolean alarmPlayed = false; 

    private static final int DEMAND_UPDATE_INTERVAL = 1;  
    private static final int DEMAND_MIN = 40;
    private static final int MAX_BATTERY_LEVEL = 150;
    private static final int POLLUTION_THRESHOLD = 100;

    public EnergyManager() {
        this. energySupply = 50;
        this.energyDemand = 50;
        this.pollutionLevel = 0;
        this.batteryLevel = 50;
        this.powerCrisis = false;
        this.overcharged = false;
        this.batteryCapacity = MAX_BATTERY_LEVEL;
        this.pollutionLevelHigh = false;
    }

    public void setGrid(Grid grid) {
    this.grid = grid;
    }

    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }


    public void addSupply(String type, Weather weather) {
        //System.out.println("addSupply() called on EnergyManager instance: " + System.identityHashCode(this));
        type = type.toLowerCase();
        double efficiency = 1.0 - (pollutionLevel / 100.0) * 0.3; // up to -30% efficiency los
        double multiplier = 1.0;

        // apply weather multiplier safely
        if (weather != null) {
            multiplier = weather.getMultiplier(type);
        }

        double finalMultiplier =  efficiency * multiplier;

        switch (type) {
            case "solar":
                energySupply += (int)(10 * finalMultiplier);
                batteryLevel += (int)(5 * finalMultiplier);
                break;

            case "wind":
                energySupply += (int)(8 * finalMultiplier);
                batteryLevel += (int)(3 * finalMultiplier);
                break;

            case "hydro":
                energySupply += (int)(12 * finalMultiplier);
                batteryLevel += (int)(4 * finalMultiplier);
                break;

            case "battery":
                batteryCapacity += 10;
                batteryLevel += 20;
                break;

            case "fossil":
                energySupply += 15;
                pollutionLevel += 15;
                break;
        }

        energySupply = Math.min(energySupply, 300);
        batteryLevel = Math.min(batteryLevel, batteryCapacity); 
        pollutionLevel = Math.min(pollutionLevel, POLLUTION_THRESHOLD); //sff function to check max pollution level,will make the board get darker later.

        
        checkCrisis();
        checkOvercharged();

        System.out.println("Type: " + type);
        System.out.println("Supply: " + energySupply + ", Battery: " + batteryLevel + ", Demand: " + energyDemand);
        System.out.println("Power Crisis: " + powerCrisis + ", Overcharged: " + overcharged);

    }

    private void checkCrisis() {
        int deficit = energyDemand - (energySupply + batteryLevel);
        powerCrisis = deficit > 10; 
    }

    private int overchargeCounter = 0;

    private void checkOvercharged() {
        if(batteryLevel > (batteryCapacity * 0.9) && energySupply > energyDemand) {
           overchargeCounter++;
           if(overchargeCounter > 5)
                overcharged = true;

            //add logic to reduce battery level gradually
            if(batteryLevel >= batteryCapacity-5){
                batteryLevel -= 5;
                batteryCapacity = Math.max(batteryCapacity - 2, 50);
            }

            //add small possibility of system blackout alotheher
            if(Math.random() < 0.05) { //5% chance
                triggerBlackout();
            }
        } else {
            overchargeCounter = 0;
            overcharged = false;
        }
    } // checks if you have exceeded safe energy levels

    private void checkBatteryDegradation() {
        // Battery is within 30 of its max capacity (unsafe zone)
        if (batteryLevel > batteryCapacity - 30) {
            // Gradual self-discharge
            batteryLevel -= 2;

            // Slow long-term wear
            if (Math.random() < 0.2) { // 20% chance per tick
                batteryCapacity = Math.max(batteryCapacity - 1, 50); 
            }
            pollutionLevel = Math.min(pollutionLevel + 1, 100);
        } 
    }

    //getter methods
    public int getEnergySupply() { return energySupply; }
    public int getEnergyDemand() { return energyDemand; }
    public int getPollutionLevel() { return pollutionLevel; }
    public int getBatteryLevel() { return batteryLevel; }
    public boolean isPowerCrisis() { return powerCrisis; }
    public boolean isOvercharged() { return overcharged; }
    public int getBatteryCapacity() { return batteryCapacity; }
    public boolean isPollutionLevelHigh() { return pollutionLevelHigh; }

    public void DemandChange() {
        demandCounter++;
        if (demandCounter < DEMAND_UPDATE_INTERVAL) return;
        demandCounter = 0;

        // Pollution penalty - slows expansion up to 40%
        double pollutionPenalty = 1.0 - ((pollutionLevel / 100.0) * 0.4);

        // Nonlinear growth - demand grows faster as supply scales up
        double supplyInfluence = Math.pow(energySupply / 100.0, 1.2); // >1 = accelerating curve
        double baseGrowth = 10 * supplyInfluence * pollutionPenalty;  // stronger baseline increase

        // Minor influence from battery capacity (industrial stability)
        baseGrowth += (batteryCapacity / 50.0);

        // Randomness to keep things organic
        double randomness = (Math.random() * 6) - 3; // -3 to +3
        baseGrowth += randomness;

        // Smoothly transition toward new higher demand
        double targetDemand = energyDemand + baseGrowth;
        energyDemand += (targetDemand - energyDemand) * 0.6;

        // Occasional surge (industrial boom)
        if (Math.random() < 0.1) {
            int spike = (int)(Math.random() * 20) + 10; // +10–30
            energyDemand += spike;
        }

        // Prevent negative or runaway values
        energyDemand = Math.max(DEMAND_MIN, energyDemand);
        energyDemand = Math.min(energyDemand, 400); // soft cap for UI scale
        energyDemand = (int)Math.round(energyDemand);

        checkCrisis();
        checkOvercharged();
        checkBatteryDegradation();
    }

    public void triggerBlackout() {
        System.out.println("System Blackout Triggered!");
        energySupply = 0;
        batteryLevel = 0;
        powerCrisis = true;

         if (grid != null) {
        grid.triggerGameOverMessage();
        }

        if (gameView != null) {
        gameView.setGameOver(true);
        }
    }

    public void pollutionAlert() {
        int stage = getPollutionStage(); 

        // Trigger alarm once when pollution gets high (stage ≥ 6)
        if (stage >= 6 && stage < 10) {
            if (!alarmPlayed) {
                Audio.playEffect("alarm.wav");
                alarmPlayed = true;
            }
        } else {
            // Reset flag if pollution drops below warning level
            alarmPlayed = false;
        }

        if (stage >= 10) {
            if (gameView != null) {
                gameView.setGameOver(true); // stops gameplay
            }
            if (grid != null) {
                grid.triggerGameOverMessage(); // enables message in draw()
            }
        }
        pollutionLevelHigh = stage >= 6;
    }

    public int getPollutionStage() {
        return pollutionLevel/10;
    }

    public void resetPollution() {
        pollutionLevel = 0;
        pollutionLevelHigh = false;
    } //resets pollution level when game is restarted

}
