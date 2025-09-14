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
import com.sokoban.juego.logica.Mapas.MapaUno;
import com.sokoban.juego.screen.MenuScreen;

public class NivelUnoScreen implements Screen, MapaBase.MapaBaseListener {

    private SpriteBatch batch;
    private Main game;
    private MapaUno mapa;
    private Texture muro, caja, objetivo, suelo, jugador, cajaObjetivo;

    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private final int GAME_WORLD_WIDTH = 800;
    private final int GAME_WORLD_HEIGHT = 480;

    public NivelUnoScreen(Main game) {
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
        try {
            muro = new Texture("muro.png");
            caja = new Texture("caja.png");
            objetivo = new Texture("objetivo.png");
            suelo = new Texture("suelo.png");
            jugador = new Texture("spritesheetmariobros.png");
            cajaObjetivo = new Texture("cajaobjetivo.png");
        } catch (Exception e) {
            System.err.println("Error cargando texturas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void inicializarNivel() {
        if (muro == null || caja == null || objetivo == null || suelo == null || jugador == null) {
            System.err.println("Error: No se pudieron cargar todas las texturas");
            return;
        }

        mapa = new MapaUno(10, 12, muro, caja, objetivo, suelo, jugador, cajaObjetivo);
        mapa.setMapaListener(this);
        mapa.cargarMapa();
        mapa.iniciarColisiones();
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar cámara
        gameCamera.update();
        batch.setProjectionMatrix(gameCamera.combined);

        // Manejar input
        if (mapa != null) {
            mapa.verificarTeclas();
            mapa.update(delta);
        }

        // Dibujar todo
        batch.begin();
        if (mapa != null) {
            mapa.dibujar(batch);
        }
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
        // El reinicio se maneja internamente en MapaBase
        System.out.println("Reiniciando nivel desde NivelUnoScreen");
    }

    @Override
    public void show() {
        System.out.println("Nivel 1 iniciado. Controles:");
        System.out.println("- Flechas: Mover");
        System.out.println("- ESC/P: Pausa");
        System.out.println("- R: Reiniciar");
        System.out.println("- TAB: Estadísticas");
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        gameCamera.position.set(GAME_WORLD_WIDTH / 2f, GAME_WORLD_HEIGHT / 2f, 0);
        gameCamera.update();
    }

    @Override
    public void pause() {
        if (mapa != null && mapa.getGestorPausa() != null) {
            mapa.getGestorPausa().pausar();
        }
    }

    @Override
    public void resume() {
        if (mapa != null && mapa.getGestorPausa() != null) {
            mapa.getGestorPausa().reanudar();
        }
    }

    @Override
    public void hide() {
        // No hacer nada específico al ocultar
    }

    @Override
    public void dispose() {
        // Detener colisiones primero
        if (mapa != null) {
            mapa.detenerColisiones();
            mapa.dispose();
            mapa = null;
        }

        // Liberar texturas
        if (muro != null) {
            muro.dispose();
            muro = null;
        }
        if (caja != null) {
            caja.dispose();
            caja = null;
        }
        if (objetivo != null) {
            objetivo.dispose();
            objetivo = null;
        }
        if (suelo != null) {
            suelo.dispose();
            suelo = null;
        }
        if (jugador != null) {
            jugador.dispose();
            jugador = null;
        }

        if (batch != null) {
            batch.dispose();
            batch = null;
        }

    }
}