package com.sokoban.juego.logica;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music; // NUEVO: Import para la música
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona la carga y reproducción de efectos de sonido y música.
 * MODIFICADO: Renombrado de SoundManager a AudioManager.
 */
public class SoundManager implements Disposable {

    // --- Singleton ---
    private static SoundManager instance;

    // --- Asset Manager y Almacenamiento ---
    public final AssetManager assetManager;
    private final Map<SoundEffect, Sound> soundCache;
    private Music currentMusic; // NUEVO: Para la pista de música actual

    private float soundVolume = 0.5f; // MODIFICADO: Nombre de variable más claro
    private float musicVolume = 0.5f; // NUEVO: Volumen separado para la música

    // --- Enum para identificar los sonidos ---
    public enum SoundEffect {
        BOSS_TERMINADO,
        // MENU_TEMA se movió a su propio Enum de música
        ERROR_MENU,
        MOVER_BLOQUE,
        NIVEL_COMPLETADO,
        PAUSA,
        SELECCION_MENU,
        SMB3_MATCH
    }

    // NUEVO: Enum específico para las pistas de música
    public enum MusicTrack {
        MENU_TEMA
    }

    // --- Constructor privado ---
    private SoundManager() {
        assetManager = new AssetManager();
        soundCache = new HashMap<>();
    }

    // --- Obtener la instancia (Singleton) ---
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // --- Métodos de Carga ---
    public void loadAssets() { // MODIFICADO: Nombre del método
        // Carga de Sonidos (Sound)
        assetManager.load("sonidos/bossTerminado.wav", Sound.class);
        assetManager.load("sonidos/errorMenu.wav", Sound.class);
        assetManager.load("sonidos/moverBloque.wav", Sound.class);
        assetManager.load("sonidos/nivelCompletado.wav", Sound.class);
        assetManager.load("sonidos/pausa.wav", Sound.class);
        assetManager.load("sonidos/seleccionMenu.wav", Sound.class);
        assetManager.load("sonidos/smb3_nspade_match.wav", Sound.class);

        // NUEVO: Carga de Música (Music). Es más eficiente para archivos largos.
        // Se recomienda usar .mp3 o .ogg para la música.
        assetManager.load("sonidos/menuPrincipal.wav", Music.class);
    }

    public boolean update() {
        if (assetManager.update()) {
            cacheSounds();
            return true;
        }
        return false;
    }

    public void cacheSounds() {
        // Solo guardamos en caché los efectos de sonido (Sound)
        soundCache.put(SoundEffect.BOSS_TERMINADO, assetManager.get("sonidos/bossTerminado.wav", Sound.class));
        soundCache.put(SoundEffect.ERROR_MENU, assetManager.get("sonidos/errorMenu.wav", Sound.class));
        soundCache.put(SoundEffect.MOVER_BLOQUE, assetManager.get("sonidos/moverBloque.wav", Sound.class));
        soundCache.put(SoundEffect.NIVEL_COMPLETADO, assetManager.get("sonidos/nivelCompletado.wav", Sound.class));
        soundCache.put(SoundEffect.PAUSA, assetManager.get("sonidos/pausa.wav", Sound.class));
        soundCache.put(SoundEffect.SELECCION_MENU, assetManager.get("sonidos/seleccionMenu.wav", Sound.class));
        soundCache.put(SoundEffect.SMB3_MATCH, assetManager.get("sonidos/smb3_nspade_match.wav", Sound.class));
    }

    // --- Métodos de Reproducción de Sonido ---
    public void play(SoundEffect effect) {
        Sound sound = soundCache.get(effect);
        if (sound != null) {
            sound.play(soundVolume);
        }
    }

    // --- NUEVO: Métodos para controlar la Música ---
    /**
     * Reproduce una pista de música.
     * @param track La pista de música a reproducir.
     * @param loop Si la música debe repetirse indefinidamente.
     */
    public void playMusic(MusicTrack track, boolean loop) {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = assetManager.get("sonidos/menuPrincipal.wav", Music.class);

        if (currentMusic != null) {
            currentMusic.setLooping(loop);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        }
    }

    /**
     * Detiene la música que se está reproduciendo actualmente.
     */
    public void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }
    }

    // --- Control de Volumen ---
    public void setSoundVolume(float v) {
        this.soundVolume = v;
    }

    public void setMusicVolume(float v) { // NUEVO
        this.musicVolume = v;
        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
        }
    }

    // --- Liberar Recursos ---
    @Override
    public void dispose() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        assetManager.dispose();
        soundCache.clear();
    }
}