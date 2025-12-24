package com.islandgrid;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class InstructionsView extends Application {

    private String currentUser = "Guest";

    public void setCurrentUser(String user) {
        this.currentUser = user;
    }

    @Override
    public void start(Stage stage) {
        Label title = new Label("⚡ Welcome to IslandGrid");
        title.setFont(new Font("Verdana", 22));

        Label userLabel = new Label("👋 Logged in as: " + currentUser);
        userLabel.setFont(new Font("Verdana", 14));

        TextArea instructions = new TextArea("""
        🌍 GAME OVERVIEW
        ----------------------------------------
        IslandGrid is a renewable-energy management simulation.
        You drop energy blocks (Solar, Wind, Hydro, Battery, Fossil)
        to balance the island’s energy supply, demand, and pollution.
        Manage resources carefully — overcharging or polluting too much
        will trigger system failure or blackouts.

        🎮 CONTROLS
        ----------------------------------------
        ▶️ LEFT / RIGHT – Move piece horizontally
        ⬇️ DOWN – Drop piece faster
        🔄 UP – Rotate clockwise
        🔁 Z – Rotate counter-clockwise
        ⏸ P – Pause or resume the game
        🔄 R – Reset the grid
        🔊 M – Mute / unmute sound
        🚨 Pollution warning siren will sound at critical levels!

        ⚙️ ENERGY MECHANICS
        ----------------------------------------
        💡 Energy Supply vs Demand:
        - Each piece adds to energy supply depending on its type and current weather.
        - Demand gradually increases as your island grows industrially.
        - If demand exceeds (supply + battery reserve) → ⚠️ Power Crisis warning.

        ⚡ Energy Supply Contributions:
        - Solar: +10 Supply, +5 Battery (boosted in ☀️ Sunny weather)
        - Wind: +8 Supply, +3 Battery (boosted in 🌬️ Windy weather)
        - Hydro: +12 Supply, +4 Battery (boosted in 🌧️ Rainy weather)
        - Battery: Expands capacity (+10) and recharges (+20)
        - Fossil: +15 Supply but +15 Pollution — use sparingly!

        🌤️ WEATHER EFFECTS
        ----------------------------------------
        Weather changes dynamically and affects generation efficiency.
        - ☀️ SUNNY → Solar ↑↑ | Wind ↔ | Hydro ↓
        - 🌬️ WINDY → Wind ↑↑ | Hydro ↑ | Solar ↔
        - 🌧️ RAINY → Hydro ↑↑ | Solar ↓ | Wind ↔
        - ☁️ CLOUDY → Solar ↓↓ | Wind ↔ | Hydro ↔
        Weather also affects background sound effects.

        🔋 BATTERY SYSTEM
        ----------------------------------------
        - Your battery stores surplus power when supply > demand.
        - Capacity starts at 150 and increases with Battery pieces.
        - Safe range: up to (capacity - 30). Beyond that → degradation.
        - If overcharged:
          • Battery begins to degrade — capacity gradually drops.
          • 5% chance of random blackout (complete power loss).
          • “⚡ Overcharged!” alert appears in HUD.
        - If battery drops too low (<15%):
          • Supply buffer weakens → system instability.

        🏭 POLLUTION & BLACKOUTS
        ----------------------------------------
        - Pollution increases mainly from Fossil energy.
        - At 60%+ → pollution alarm sounds (warning).
        - At 100% → game ends in blackout.
        - High pollution also reduces renewable efficiency by up to -30%.
        - Blackouts reset battery to 0 and stop all generation.

        📊 STATUS BARS
        ----------------------------------------
        Right-hand HUD shows:
        - Energy Supply (orange/red)
        - Energy Demand (blue)
        - Battery Level (green/yellow/red)
        - Pollution (black)
        Alerts appear for:
        ⚠️ Power Crisis – Demand exceeds Supply+Battery.
        ⚡ Overcharged – Battery dangerously high.
        🔕 Mute Icon – Shows when audio is disabled.

        🔊 AUDIO SYSTEM
        ----------------------------------------
        - Background music loops softly during play.
        - Sound effects:
            • move.wav – when pieces shift
            • rotate.wav – when rotating
            • lock.wav – when piece lands
            • birds.wav / gusts.wav / rain.wav / lowwind.wav – weather ambience
            • alarm.wav – high pollution
            • blackout.wav – system shutdown
        - M toggles mute for both music and effects.

        🧠 STRATEGY TIPS
        ----------------------------------------
        - Keep your Supply slightly above Demand.
        - Use Battery pieces to balance peaks in Demand.
        - Avoid relying heavily on Fossil fuel.
        - Watch weather changes — they alter production.
        - Restart (R) strategically to plan new builds.

        🎯 WIN CONDITION
        ----------------------------------------
        Survive as long as possible without blackouts or overcharge.
        Maintain balance between renewable energy, demand growth,
        and environmental impact.

        🌎 Remember: sustainability is key to keeping the lights on!
        """);

        instructions.setWrapText(true);
        instructions.setEditable(false);
        instructions.setFont(new Font("Consolas", 13));
        instructions.setStyle("-fx-control-inner-background: beige; -fx-border-color: saddlebrown;");

        Button startBtn = new Button("🚀 Start Simulation");
        startBtn.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        startBtn.setOnAction(e -> {
            GameView game = new GameView();
            game.setCurrentUser(currentUser);
            try {
                game.start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(15, title, userLabel, instructions, startBtn);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: wheat; -fx-alignment: center;");

        Button backButton = new Button("⬅ Back to Menu");
        backButton.setOnAction(e -> {
            WelcomeMenu menu = new WelcomeMenu();
            try {
                menu.start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        layout.getChildren().add(backButton);
        Scene scene = new Scene(layout, 700, 620);
        stage.setScene(scene);
        stage.setTitle("IslandGrid Instructions");
        stage.show();
    }
}
