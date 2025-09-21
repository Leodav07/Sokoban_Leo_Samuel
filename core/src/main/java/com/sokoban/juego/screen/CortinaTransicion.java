package com.sokoban.juego.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;

public class CortinaTransicion implements Screen {
    private final Game game;
    private final Screen oldScreen;   // pantalla que estaba antes
    private final Screen nextScreen;  // pantalla nueva

    private Texture cortina;
    private float yPos;
    private float velocidad = 800; // velocidad px/seg
    private boolean bajando = true;
    private boolean subiendo = false;
    private boolean transicionCompleta = false;

    private SpriteBatch batch;

    private int screenWidth;
    private int screenHeight;

    public CortinaTransicion(Game game, Screen oldScreen, Screen nextScreen) {
        this.game = game;
        this.oldScreen = oldScreen;
        this.nextScreen = nextScreen;

        cortina = new Texture("menu/cortina.png");
       cortina.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        batch = new SpriteBatch();
        
        
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        // Cortina empieza arriba, fuera de la pantalla
        yPos = screenHeight;
        
        // NO llamar game.setScreen(nextScreen) aquí!
        
        // La transición debe manejar cuándo cambiar
        game.setScreen(nextScreen);
        
   
    }

    @Override
    public void render(float delta) {
        // Prevenir que se ejecute si la transición ya terminó
        if (transicionCompleta) {
            return;
        }
        
 // 1. Dibujar pantalla activa
        if (bajando) {
            oldScreen.render(delta);   // mientras baja, se ve la vieja
        } else {
           nextScreen.render(Gdx.graphics.getDeltaTime()); 
        }

        // 2. Dibujar cortina encima (cubre todo)
        batch.begin();
        batch.draw(cortina,
                0, yPos,
                screenWidth, screenHeight);
        batch.end();

        // 3. Animación
        if (bajando) {
            yPos -= velocidad * delta;
            if (yPos <= 0) {
                // cortina tapó todo → ahora empezamos a subir
                yPos = 0;
                bajando = false;
                subiendo = true;
            }
        } else if (subiendo) {
            yPos += velocidad * delta;
            if (yPos >= screenHeight) {
                yPos = screenHeight;
                subiendo = false;
                transicionCompleta = true;

                // Ahora sí cambiar definitivamente a la nueva pantalla
                finalizarTransicion();
            }
        }
    }
    
    private boolean nextScreenInitialized = false;
    
    private void finalizarTransicion() {
        // Ocultar la pantalla antigua
        if (oldScreen != null) {
            oldScreen.dispose();
        }
        
        // Cambiar a la nueva pantalla
        game.setScreen(nextScreen);
        // Limpiar recursos de la transición
        dispose();
    }

    @Override 
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        
        // Propagar resize a la pantalla activa
        if (bajando && oldScreen != null) {
            oldScreen.resize(width, height);
        } else if (subiendo && nextScreen != null && nextScreenInitialized) {
            nextScreen.resize(width, height);
        }
    }
    
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        if (cortina != null) {
            //cortina.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}