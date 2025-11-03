package com.islandgrid;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class Audio {
    private static MediaPlayer bgmPlayer;
    private static double musicVolume = 0.1;
    private static double effectVolume = 0.3;

    // Play short .wav effects
    public static void playEffect(String name) {
        URL resource = Audio.class.getResource("/resources/sounds/" + name);
        if (resource != null) {
            AudioClip clip = new AudioClip(resource.toExternalForm());
            clip.setVolume(effectVolume);
            clip.play();
        } else {
            System.err.println("Sound not found: " + name);
        }
    }

    // Play or loop background music
    public static void playMusic(String name, boolean loop) {
        URL resource = Audio.class.getResource("/resources/sounds/" + name);
        if (resource != null) {
            if (bgmPlayer != null) bgmPlayer.stop();
            Media media = new Media(resource.toExternalForm());
            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setVolume(musicVolume);
            if (loop) bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.play();
        } else {
            System.err.println("Music not found: " + name);
        }
    }

    public static void stopMusic() {
        if (bgmPlayer != null) bgmPlayer.stop();
    }

    //Getters and Setters for volume
    public static double getMusicVolume() { return musicVolume; }
    public static double getEffectVolume() {return effectVolume; }

    public static void setMusicVolume(double volume) {
        musicVolume = Math.max(0, Math.min(1, volume));
        if (bgmPlayer != null) bgmPlayer.setVolume(musicVolume);
    }

    public static void setEffectVolume(double volume) {
        effectVolume = Math.max(0, Math.min(1, volume));
    }
}
