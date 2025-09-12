/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.niveles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.Mapas.MapaBase;
import com.sokoban.juego.logica.Mapas.MapaUno;
import com.sokoban.juego.screen.MenuScreen;

/**
 *
 * @author hnleo
 */
public class NivelUnoScreen implements Screen, MapaBase.MapaBaseListener  {

    SpriteBatch batch;
    private Main game;
    private MapaUno mapa;
    private Texture muro, caja, objetivo, suelo, jugador;

    public NivelUnoScreen(Main game) {
        this.game = game;
       batch = new SpriteBatch();
        cargarRecursos();
        inicializarNivel();
    }
    
    private void cargarRecursos() {
        muro = new Texture("muro.png");
        caja = new Texture("caja.png");
        objetivo = new Texture("objetivo.png");
        suelo = new Texture("suelo.png");
        jugador = new Texture("jugador.png");
    }
    
    private void inicializarNivel() {
        mapa = new MapaUno(10, 12, muro, caja, objetivo, suelo, jugador);
        mapa.setMapaListener(this); 
        mapa.cargarMapa();
        mapa.iniciarColisiones();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        mapa.update(delta);
        
        batch.begin();
        mapa.dibujar(batch);
        batch.end();
    }

    @Override
    public void onVolverMenuPrincipal() {
        Gdx.app.postRunnable(() -> {
        game.setScreen(new MenuScreen(game));
        dispose();
    });
    }

    @Override
    public void onSalirJuego() {
        Gdx.app.postRunnable(() -> {
        dispose();
        Gdx.app.exit();
    });
    }

    @Override
    public void onReiniciarNivel() {
        // Reiniciar el nivel actual
        dispose();
        game.setScreen(new NivelUnoScreen(game));
    }

    @Override
    public void show() {
        System.out.println("Nivel 1 iniciado. Presiona ESC o P para pausar.");
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
        if (mapa != null && mapa.getGestorPausa() != null) {
            mapa.getGestorPausa().pausar();
        }
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (mapa != null) {
            mapa.detenerColisiones();
            mapa.dispose();
        }
        
        if (muro != null) muro.dispose();
        if (caja != null) caja.dispose();
        if (objetivo != null) objetivo.dispose();
        if (suelo != null) suelo.dispose();
        if (jugador != null) jugador.dispose();
    }
}
