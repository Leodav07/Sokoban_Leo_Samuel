package com.sokoban.juego.niveles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.Caja;
import com.sokoban.juego.logica.EntradaGestor;
import com.sokoban.juego.logica.MapLoader;
import com.sokoban.juego.logica.Jugador;
import com.sokoban.juego.logica.TileMapRun;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NivelUno implements Screen {

    private Main game;
    private OrthographicCamera camara;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private List<Caja> cajas;
    private Jugador jugador;
    private TileMapRun tileMap;
    private EntradaGestor inputHandler;

    private MapLoader mapLoader;
    private CompletableFuture<Void> mapLoadingProceso;
    private String msgLoading = "Cargando el mapa...";
    private boolean cajasYaCargadas = false;
    private boolean jugadorYaCargado = false;
    private boolean tileMapYaCargado = false;

    public NivelUno(Main game) {
        this.game = game;
        this.cajas = new ArrayList<>(); 
        create();
    }

    private void create() {
        System.out.println("Iniciando nivel uno..");

        setupCamara();
        setupGraphics();

        mapLoader = new MapLoader();
        System.out.println("MapLoader creado: " + (mapLoader != null ? "OK" : "ERROR"));

        loadMap();

        System.out.println("Nivel uno inicializado exitosamente");
    }

    private void setupGraphics() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        System.out.println("Gráficos configurados");
    }

    private void setupCamara() {
        camara = new OrthographicCamera();
        viewport = new FitViewport(768, 640, camara);
        camara.position.set(768 / 2f, 640 / 2f, 0);
        camara.update();
    }

    private void loadMap() {
        System.out.println("Iniciando la carga del mapa...");

        if (mapLoader == null) {
            msgLoading = "Error: MapLoader no inicializado";
            return;
        }

        String mapPath = "mapas/mapanivel1.tmx";
        System.out.println("Intentando cargar: " + mapPath);

        if (!Gdx.files.internal(mapPath).exists()) {
            System.out.println("ERROR: El archivo " + mapPath + " no existe");
            msgLoading = "Error: Archivo de mapa no encontrado";
            return;
        }

        try {
            CompletableFuture<Void> loadingFuture = mapLoader.loadMapAsync(mapPath);
            
            loadingFuture.whenComplete((result, exception) -> {
                if (exception != null) {
                    System.err.println("Error al cargar el mapa: " + exception.getMessage());
                    msgLoading = "Error al cargar mapa";
                } else {
                    Gdx.app.postRunnable(() -> {
                        try {
                            this.tileMap = mapLoader.crearTileMap(mapLoader.getTiledMap());
                            tileMapYaCargado = true;
                            
                            this.cajas = mapLoader.cargarCajas(mapLoader.getTiledMap());
                            cajasYaCargadas = true;
                            
                            this.jugador = mapLoader.cargarJugador(mapLoader.getTiledMap());
                            jugadorYaCargado = true;
                            
                            this.inputHandler = new EntradaGestor(jugador, tileMap);
                            
                            msgLoading = "Juego listo";
                        } catch (Exception e) {
                            System.out.println("Error al cargar elementos del juego: " + e.getMessage());
                            e.printStackTrace();
                            msgLoading = "Error al cargar elementos del juego";
                        }
                    });
                }
            });

        } catch (Exception e) {
            System.out.println("Error al cargar el mapa: " + e.getMessage());
            e.printStackTrace();
            msgLoading = "Excepción al cargar mapa";
        }
    }

    @Override
    public void show() {
        System.out.println("Nivel uno mostrado");
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.2f, 0.3f, 0.4f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camara.update();

        if (mapLoader != null && mapLoader.isMapLoaded()) {
            mapLoader.render(camara);
        }

        batch.setProjectionMatrix(camara.combined);
        batch.begin();
        
        if (cajasYaCargadas && cajas != null && !cajas.isEmpty()) {
            for (Caja caja : cajas) {
                caja.render(batch);
            }
        }
        
        if (jugadorYaCargado && jugador != null) {
            jugador.render(batch);
        }
        
        batch.end();

    }

    public void update(float delta) {
        if (mapLoader != null) {
            mapLoader.update(delta);
        }
        
        if (jugadorYaCargado && tileMapYaCargado) {
            if (inputHandler != null) {
                inputHandler.update(delta);
            }
            if (jugador != null) {
                jugador.update(delta);
            }
        }
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camara.setToOrtho(false, width, height);
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
        System.out.println("Liberando recursos de Nivel uno...");

        if (cajas != null) {
            for (Caja caja : cajas) {
                caja.dispose();
            }
            cajas.clear();
        }
        
        if (jugador != null) {
            jugador.dispose();
            jugador = null;
        }

        if (mapLoader != null) {
            mapLoader.dispose();
            mapLoader = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }

        System.out.println("Nivel uno disposed correctamente");
    }
}