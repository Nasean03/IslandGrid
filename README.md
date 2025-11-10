# 🌴 IslandGrid: Renewable Energy Puzzle Simulator  
[![GitHub Repo](https://img.shields.io/badge/GitHub-IslandGrid-181717?style=flat&logo=github)](https://github.com/Nasean03/IslandGrid)

**IslandGrid** is a JavaFX-based simulation game that challenges players to balance renewable and non-renewable energy sources on an island grid.  
Inspired by Tetris-style mechanics, the game blends puzzle logic with environmental sustainability, encouraging smarter energy decisions and awareness of pollution and storage dynamics.

---

## ⚡ Features
- 🎮 Interactive gameplay with falling renewable energy pieces (solar, wind, hydro, fossil, battery)  
- ☁️ Dynamic weather system affecting power generation  
- 🔋 Energy manager system tracking supply, demand, battery, and pollution  
- 🔊 Integrated sound effects and music  
- 🧠 Simulation logic with live feedback via status bars  
- 🧾 User login system using MySQL database  
- 🌍 Future plan: integrate real-world renewable data and machine learning  

---

## 🧰 Technologies Used
- **Language:** Java  
- **Framework:** JavaFX 21  
- **Database:** MySQL (local via WAMP)  
- **Build Tool:** Maven  
- **Audio:** JavaFX Media API  
- **Deployment:** InfinityFree (HTML demo page)  

---

## 🚀 Installation & Setup
**Prerequisites:** Java 21+, JavaFX SDK 21+, MySQL (WAMP or XAMPP), Maven installed.  

1. Clone the repository and enter directory:  
   ```bash
   git clone https://github.com/Nasean03/IslandGrid.git
   cd IslandGrid
2. Create database and table:
   ```sql
    CREATE DATABASE islandgrid;
    USE islandgrid;
    
    CREATE TABLE users (
      id INT PRIMARY KEY AUTO_INCREMENT,
      username VARCHAR(50) UNIQUE,
      password VARCHAR(255)
    );
   
3. Add DB credentials to DatabaseManager.java.

4. Run using Maven:
    ```bash
    mvn clean javafx:run

---

## 🎥 Demo

Coming soon!!!!

---

## 🧩 Future Improvements

- 🌦️ Integrate real-world weather data APIs (e.g., solar & wind datasets)
- 🤖 Add machine learning models for generation forecasting and grid optimization
- 🪵 Include a manual “wooden placement” mode (strategic version)
- ☁️ Improved UI themes and sound design
- 🌐 Web companion dashboard for player stats

--- 

## 👨‍💻 Author

🎓 **BSc Computer Science**, The University of the West Indies – Cave Hill

🌐 [**Portfolio Website**](https://naseanbelgrave.infinityfree.me/islandgrid.html)  
💼 [**LinkedIn**](https://bb.linkedin.com/in/nasean-belgrave-55209b220)



   
