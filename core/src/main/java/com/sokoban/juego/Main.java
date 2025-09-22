package com.sokoban.juego;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sokoban.juego.logica.SoundManager;
import com.sokoban.juego.screen.InicioScreen;
import com.sokoban.juego.screen.LoginScreen;
import java.util.Locale;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {

    public I18NBundle bundle;
    private float vol = 0.5f;

    @Override
    public void create() {
        
        
        bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/messages"));
        
         // Inicia la carga de sonidos
        SoundManager.getInstance().loadAssets();
        
        // --- NOTA IMPORTANTE ---
        // Aquí deberías cambiar a una "LoadingScreen" que llame a SoundManager.getInstance().update()
        // en su método render(). Cuando update() devuelva 'true', cambias a LoginScreen.
        // Por ahora, para que funcione, vamos a forzar la carga completa de forma síncrona.
        SoundManager.getInstance().assetManager.finishLoading(); // ¡Esto puede congelar el juego al inicio!
        SoundManager.getInstance().cacheSounds(); // Carga los sonidos en el mapa
        SoundManager.getInstance().playMusic(SoundManager.MusicTrack.MENU_TEMA, true);
        setScreen(new InicioScreen(this));
    }
    
    public void setVolumen(float v){
        vol = v;
    }
    
    public float getVolumen(){
        return vol;
    }
}
