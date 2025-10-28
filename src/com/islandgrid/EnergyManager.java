package com.islandgrid;

//import javafx.scene.paint.Color;

public class EnergyManager {
    private int energySupply;
    private int energyDemand;
    private int pollutionLevel;
    private int batteryLevel;
    private boolean powerCrisis;
    private boolean overcharged;
    private int batteryCapacity; 
    private Grid grid;
    private GameView gameView;
    private boolean pollutionLevelHigh;


    private static final int MAX_BATTERY_LEVEL = 150;
    private static final int POLLUTION_THRESHOLD = 100;
    private static final int MAX_SAFE_BATTERY = 120;

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


    public void addSupply(String type) {
        System.out.println("addSupply() called on EnergyManager instance: " + System.identityHashCode(this));
        type = type.toLowerCase();


          switch (type) {
            case "solar":
                energySupply += 10;
                batteryLevel += 5;
                break;
                
            
            case "wind":
                energySupply += 8;
                batteryLevel += 3;
                break;
               

            case "hydro":
                energySupply += 12;
                batteryLevel += 4;
                break;
                
            case "battery":
                batteryCapacity += 10;
                batteryLevel = batteryLevel + 20; 
                break;
              
            case "fossil":
                 energySupply += 15;
                pollutionLevel += 15;
                break;       
        }

        energySupply = Math.min(energySupply, 150);
        batteryLevel = Math.min(batteryLevel, batteryCapacity); 
        pollutionLevel = Math.min(pollutionLevel, POLLUTION_THRESHOLD); //sff function to check max pollution level,will make the board get darker later.

        checkCrisis();
        checkOvercharged();

        System.out.println("Type: " + type);
        System.out.println("Supply: " + energySupply + ", Battery: " + batteryLevel + ", Demand: " + energyDemand);
        System.out.println("Power Crisis: " + powerCrisis + ", Overcharged: " + overcharged);

    }

    private void checkCrisis() {
        if(energySupply + batteryLevel < energyDemand) {
            powerCrisis = true;
        } else {
            powerCrisis = false;
        }
    }//checks if you are in a power crisis (under power demand)

     private void checkOvercharged() {
        if(batteryLevel > (batteryCapacity-30) && energySupply > energyDemand + 20) {
            overcharged = true;
            //add logic to reduce battery level gradually
            if(batteryLevel >= batteryCapacity-5){
                batteryLevel -= 5;
                batteryCapacity -= 2;
            }

            //add small possibility of system blackout alotheher
            if(Math.random() < 0.05) { //5% chance
                triggerBlackout();
            }
        } else {
            overcharged = false;
        }
    } // checks if you have exceeded safe energy levels

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
        int change = (int)(Math.random() * 21) - 10; // Random change between -10 and +10
        energyDemand = Math.max(0, energyDemand + change); // Ensure demand doesn't go negative
        checkCrisis();
        checkOvercharged();
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
