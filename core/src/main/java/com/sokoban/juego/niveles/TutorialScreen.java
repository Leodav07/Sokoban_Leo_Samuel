/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.niveles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.Mapas.MapaBase;
import com.sokoban.juego.logica.Mapas.MapaTutorial;
import com.sokoban.juego.screen.CortinaTransicion;
import com.sokoban.juego.screen.LvlSelectScreen;
import com.sokoban.juego.screen.MenuScreen;

/**
 *
 * @author hnleo
 */
public class TutorialScreen implements Screen, MapaBase.MapaBaseListener {

    private SpriteBatch batch;
    private Main game;
    private MapaTutorial mapa;
    private Texture muro, caja, objetivo, suelo, jugador, cajaObjetivo, fondo;

    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private final int GAME_WORLD_WIDTH = 800;
    private final int GAME_WORLD_HEIGHT = 480;

    public TutorialScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        inicializarCamara();
        cargarRecursos();
        inicializarNivel();
    }

    private void inicializarCamara() {
        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(GAME_WORLD_WIDTH, GAME_WORLD_HEIGHT, gameCamera);
        gameViewport.apply(true);
    }

    private void cargarRecursos() {
        muro = new Texture("muro.png");
        caja = new Texture("caja.png");
        objetivo = new Texture("objetivo.png");
        suelo = new Texture("suelo.png");
        jugador = new Texture("spritesheetmariobros.png");
        cajaObjetivo = new Texture("cajaobjetivo.png");
        fondo = new Texture("fondo.png");
    }

    private void inicializarNivel() {
        mapa = new MapaTutorial(muro, caja, objetivo, suelo, jugador, cajaObjetivo, fondo, game);
        mapa.setMapaListener(this);
        mapa.cargarMapa();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameCamera.update();
        batch.setProjectionMatrix(gameCamera.combined);

        mapa.update(delta);

        batch.begin();
        mapa.dibujar(batch);
        batch.end();
    }

    @Override
    public void onNivelFinalizado() {
        Gdx.app.postRunnable(() -> {
            game.setScreen(new CortinaTransicion(game, this, new LvlSelectScreen(game)));
        });
    }

    @Override
    public void onVolverMenuPrincipal() {
    }

    @Override
    public void onSalirJuego() {
    }

    @Override
    public void onReiniciarNivel() {
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int w, int h) {
        gameViewport.update(w, h, true);
    }

    @Override
    public void pause() {
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
            mapa.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        muro.dispose();
        caja.dispose();
        objetivo.dispose();
        suelo.dispose();
        jugador.dispose();
        cajaObjetivo.dispose();
        fondo.dispose();
    }
}
