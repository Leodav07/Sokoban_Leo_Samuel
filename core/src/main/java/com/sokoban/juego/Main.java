package com.sokoban.juego;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ScreenUtils;
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
        setScreen(new LoginScreen(this));
    }
    
    public void setVolumen(float v){
        vol = v;
    }
    
    public float getVolumen(){
        return vol;
    }
}
