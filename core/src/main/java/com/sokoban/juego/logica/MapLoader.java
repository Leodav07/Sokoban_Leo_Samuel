package com.sokoban.juego.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MapLoader {

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TmxMapLoader mapLoader;
    private int tileWidth;
    private int tileHeight;
    private int mapWidth;   
    private int mapHeight;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private boolean isMapLoaded = false;
    private boolean isLoading = false;
    private String loadingStatus = "Esperando...";

    private volatile FileHandle mapFileHandle;
    private volatile String mapDataXML;
    private CompletableFuture<Void> loadingFuture;

    public MapLoader() {
        this.mapLoader = new TmxMapLoader();
    }

    public CompletableFuture<Void> loadMapAsync(String mapPath) {
        if (isLoading || isMapLoaded) {
            return CompletableFuture.completedFuture(null);
        }

        isLoading = true;
        loadingStatus = "Iniciando carga...";

        loadingFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                loadingStatus = "Leyendo archivo...";

                Thread.sleep(500);

                mapFileHandle = Gdx.files.internal(mapPath);

                if (!mapFileHandle.exists()) {
                    throw new RuntimeException("Archivo no encontrado: " + mapPath);
                }

                mapDataXML = mapFileHandle.readString();

                loadingStatus = "Archivo leído, preparando recursos...";

                Gdx.app.postRunnable(() -> {
                    try {
                        loadingStatus = "Creando texturas...";

                        this.tiledMap = mapLoader.load(mapPath);
                        this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

                        // Obtener propiedades del mapa
                        MapProperties properties = tiledMap.getProperties();
                        this.tileWidth = properties.get("tilewidth", Integer.class);
                        this.tileHeight = properties.get("tileheight", Integer.class);
                        this.mapWidth = properties.get("width", Integer.class);
                        this.mapHeight = properties.get("height", Integer.class);

                        this.isMapLoaded = true;
                        this.isLoading = false;
                        loadingStatus = "¡Cargado exitosamente!";

                        loadingFuture.complete(null);

                    } catch (Exception e) {
                        System.err.println("Error al crear recursos: " + e.getMessage());
                        handleLoadingError(e);
                    }
                });

            } catch (Exception e) {
                System.err.println("Error en lectura: " + e.getMessage());
                handleLoadingError(e);
            }
        }, executorService);

        return loadingFuture;
    }

    public Future<Void> loadMapWithProgress(String mapPath, ProgressCallback callback) {
        if (isLoading || isMapLoaded) {
            return CompletableFuture.completedFuture(null);
        }

        return executorService.submit(() -> {
            try {
                isLoading = true;

                if (callback != null) {
                    callback.onProgress(10, "Validando archivo...");
                }
                Thread.sleep(100);

                FileHandle file = Gdx.files.internal(mapPath);
                if (!file.exists()) {
                    throw new RuntimeException("Archivo no encontrado: " + mapPath);
                }

                if (callback != null) {
                    callback.onProgress(50, "Leyendo datos del mapa...");
                }
                Thread.sleep(300);
                String xmlData = file.readString();

                if (callback != null) {
                    callback.onProgress(80, "Preparando recursos...");
                }
                Thread.sleep(200);

                CompletableFuture<Void> mainThreadTask = new CompletableFuture<>();

                Gdx.app.postRunnable(() -> {
                    try {
                        if (callback != null) {
                            callback.onProgress(100, "Creando texturas...");
                        }

                        this.tiledMap = mapLoader.load(mapPath);
                        this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

                        this.isMapLoaded = true;
                        this.isLoading = false;

                        if (callback != null) {
                            callback.onComplete();
                        }
                        mainThreadTask.complete(null);

                    } catch (Exception e) {
                        if (callback != null) {
                            callback.onError(e);
                        }
                        mainThreadTask.completeExceptionally(e);
                    }
                });

                mainThreadTask.get();

            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
                handleLoadingError(e);
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private void handleLoadingError(Exception e) {
        this.isLoading = false;
        this.loadingStatus = "Error: " + e.getMessage();
        e.printStackTrace();

        if (loadingFuture != null && !loadingFuture.isDone()) {
            loadingFuture.completeExceptionally(e);
        }
    }

    public void render(OrthographicCamera camera) {
        if (isMapLoaded && mapRenderer != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        }
    }

    public void update(float deltaTime) {
        if (isMapLoaded) {
        }
    }

    public void dispose() {
        if (tiledMap != null) {
            tiledMap.dispose();
            tiledMap = null;
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
            mapRenderer = null;
        }

        isMapLoaded = false;
        isLoading = false;
    }

    public static void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getLoadingStatus() {
        return loadingStatus;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }   

    public int getTileHeight() {
        return this.tileHeight;
    }

    public int getMapWidthInTiles() {
        return this.mapWidth;
    }

    public int getMapHeightInTiles() {
        return this.mapHeight;
    }

    public OrthogonalTiledMapRenderer getMapRenderer() {
        return mapRenderer;
    }
    
    public List<Caja> cargarCajas(TiledMap mapa){
        List<Caja> cajas = new ArrayList<>();
        
        MapLayer capaCajas = mapa.getLayers().get("cajas");
        if(capaCajas!=null){
            for(MapObject ob : capaCajas.getObjects()){
                if(ob instanceof RectangleMapObject){
                    Rectangle rect = ((RectangleMapObject) ob).getRectangle();
                    Caja caja = new Caja(rect.x, rect.y);
                    cajas.add(caja);
                }
            }
        }
        return cajas;
    }
    
    public TileMapRun crearTileMap(TiledMap mapa) {
        if (mapa == null) return null;
        
        MapProperties properties = mapa.getProperties();
        int mapWidth = properties.get("width", Integer.class);
        int mapHeight = properties.get("height", Integer.class);
        int tileWidth = properties.get("tilewidth", Integer.class);
        int tileHeight = properties.get("tileheight", Integer.class);
        
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        
        TileMapRun tileMapRun = new TileMapRun(mapWidth, mapHeight, tileWidth, tileHeight);
        
        MapLayer capaColisiones = mapa.getLayers().get("colliders");
        if (capaColisiones != null) {
            for (MapObject objeto : capaColisiones.getObjects()) {
                if (objeto instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) objeto).getRectangle();
                    int tileX = (int) (rect.x / tileWidth);
                    int tileY = (int) (rect.y / tileHeight);
                    
                    if (tileMapRun.dentroInside(tileX, tileY)) {
                        String tipo = objeto.getProperties().get("tipo", "muro", String.class);
                        boolean solido = objeto.getProperties().get("solido", true, Boolean.class);
                        boolean movible = objeto.getProperties().get("movible", false, Boolean.class);
                        boolean objetivo = objeto.getProperties().get("objetivo", false, Boolean.class);
                        
                        tileMapRun.grid[tileX][tileY] = new TileCell(tileX, tileY, solido, movible, objetivo, tipo);
                    }
                }
            }
        }
        
        
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (tileMapRun.grid[x][y] == null) {
                    tileMapRun.grid[x][y] = new TileCell(x, y, false, false, false, "suelo");
                }
            }
        }
        
        return tileMapRun;
    }
    
    
    
    public Jugador cargarJugador(TiledMap mapa) {
        MapLayer capaJugador = mapa.getLayers().get("jugador");
        if (capaJugador != null) {
            for (MapObject objeto : capaJugador.getObjects()) {
                if (objeto instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) objeto).getRectangle();
                    return new Jugador(rect.x, rect.y);
                }
            }
        }
        
        return new Jugador(64, 64);
    }

    public interface ProgressCallback {

        void onProgress(int percentage, String status);

        void onComplete();

        void onError(Exception e);
    }
}