package com.sokoban.juego.logica;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

public class SoundManager implements Disposable {

    private static SoundManager instance;
    public final AssetManager assetManager;
    private final Map<SoundEffect, Sound> soundCache;
    private Music currentMusic;
    private float soundVolume = 1f;
    private float musicVolume = 0.5f;

    public enum SoundEffect {
        BOSS_TERMINADO,
        GUARDADO,
        // NIVEL_TEMA se movió a MusicTrack
        ERROR_MENU,
        MOVER_BLOQUE,
        NIVEL_COMPLETADO,
        PAUSA,
        SELECCION_MENU,
        SMB3_MATCH
    }

    // <<-- CAMBIO: El enum de música ahora maneja múltiples pistas con sus rutas -->>
    public enum MusicTrack {
        MENU_TEMA("sonidos/menuPrincipal.wav"),
        NIVEL_TEMA("sonidos/nivelTema.mp3"); // Asumiendo que el archivo se llama así

        private final String filePath;

        MusicTrack(String filePath) {
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }
    }
    
    private SoundManager() {
        assetManager = new AssetManager();
        soundCache = new HashMap<>();
    }
    
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    public void loadAssets() {
        assetManager.load("sonidos/bossTerminado.wav", Sound.class);
        assetManager.load("sonidos/errorMenu.wav", Sound.class);
        assetManager.load("sonidos/guardado.wav", Sound.class);
        assetManager.load("sonidos/moverBloque.wav", Sound.class);
        assetManager.load("sonidos/nivelCompletado.wav", Sound.class);
        assetManager.load("sonidos/pausa.wav", Sound.class);
        assetManager.load("sonidos/seleccionMenu.wav", Sound.class);
        assetManager.load("sonidos/smb3_nspade_match.wav", Sound.class);

        // <<-- CAMBIO: Se cargan todas las pistas de música definidas en el enum -->>
        for (MusicTrack track : MusicTrack.values()) {
            assetManager.load(track.getFilePath(), Music.class);
        }
    }
    
    public boolean update() {
        if (assetManager.update()) {
            cacheSounds();
            return true;
        }
        return false;
    }

    public void cacheSounds() {
        // <<-- CAMBIO: Se elimina NIVEL_TEMA de la caché de sonidos -->>
        soundCache.put(SoundEffect.BOSS_TERMINADO, assetManager.get("sonidos/bossTerminado.wav", Sound.class));
        soundCache.put(SoundEffect.ERROR_MENU, assetManager.get("sonidos/errorMenu.wav", Sound.class));
        soundCache.put(SoundEffect.GUARDADO, assetManager.get("sonidos/guardado.wav", Sound.class));
        soundCache.put(SoundEffect.MOVER_BLOQUE, assetManager.get("sonidos/moverBloque.wav", Sound.class));
        soundCache.put(SoundEffect.NIVEL_COMPLETADO, assetManager.get("sonidos/nivelCompletado.wav", Sound.class));
        soundCache.put(SoundEffect.PAUSA, assetManager.get("sonidos/pausa.wav", Sound.class));
        soundCache.put(SoundEffect.SELECCION_MENU, assetManager.get("sonidos/seleccionMenu.wav", Sound.class));
        soundCache.put(SoundEffect.SMB3_MATCH, assetManager.get("sonidos/smb3_nspade_match.wav", Sound.class));
    }
    
    public void play(SoundEffect effect) {
        Sound sound = soundCache.get(effect);
        if (sound != null) {
            sound.play(soundVolume);
        }
    }

    // <<-- CAMBIO: El método ahora usa la ruta del enum para reproducir la música correcta -->>
    public void playMusic(MusicTrack track, boolean loop) {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = assetManager.get(track.getFilePath(), Music.class);

        if (currentMusic != null) {
            currentMusic.setLooping(loop);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        }
    }
    
    public void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }
    }

    public void setSoundVolume(float v) {
        this.soundVolume = v;
    }

    public void setMusicVolume(float v) {
        this.musicVolume = v;
        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
        }
    }
    
       public void setGlobalVolume(float volume) {
        setSoundVolume(volume);
        setMusicVolume(volume);
    }
    
    @Override
    public void dispose() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        assetManager.dispose();
        soundCache.clear();
    }
}