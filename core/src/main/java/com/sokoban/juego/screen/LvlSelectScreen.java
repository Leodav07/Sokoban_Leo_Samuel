/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.Nivel;
import com.sokoban.juego.logica.accounts.GestorProgreso;
import com.sokoban.juego.niveles.NivelUno;

/**
 *
 * @author unwir
 */
public class LvlSelectScreen implements Screen {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private BitmapFont font;

    private enum PlayerState {
        IDLE, MOVING
    }
    private PlayerState pS = PlayerState.IDLE;

    private Nivel[] niveles;
    private int[][] conexiones; // [nivel_origen][nivel_destino]
    private int nivelSeleccionadoIndex;

    // Texturas para decoración del mapa
    private Texture fondoMapa;
    private Texture iconoCaja;
    private Texture iconoMeta;

    private Texture playerTexture;
    private Animation<TextureRegion> playerAnimation;
    private float stateTime = 0f; // Para controlar el tiempo de la animación

    // Posición LÓGICA del jugador en el grid (casillas)
    private int playerGridX;
    private int playerGridY;

    // --- NUEVAS VARIABLES PARA LA ANIMACIÓN DEL MOVIMIENTO ---
// Posición VISUAL del jugador en píxeles (para el movimiento suave)
    private Vector2 playerVisualPosition = new Vector2();
// Vectores para saber desde y hacia dónde animar
    private Vector2 moveFrom = new Vector2();
    private Vector2 moveTo = new Vector2();
// Temporizador para la animación
    private float moveTimer = 0f;
// Duración de la animación en segundos (ajústalo a tu gusto)
    private final float MOVE_DURATION = 0.2f;
    // Progreso del jugador
    private boolean[] nivelesCompletados;
    private Main game;

    private Viewport vp;

    private boolean[][] esCamino;
    private TiledMap tiledMap;
    private TmxMapLoader mapLoader;
    private OrthogonalTiledMapRenderer mapRenderer;

    private final int TILE_SIZE = 16;

    public LvlSelectScreen(Main game) {
        this.game = game;
        inicializar();
        crearMapa();
        cargarProgreso();
    }

    private void inicializar() {

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false, 320, 160);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);

        camera.update();

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);

        niveles = new Nivel[7];
        nivelesCompletados = new boolean[8]; // índice 0 no se usa
        nivelSeleccionadoIndex = 0; // Primer nivel

        // Definir conexiones entre niveles (nivel origen -> nivel destino)
        conexiones = new int[][]{
            {1, 2}, // Nivel 1 conecta con nivel 2
            {2, 3}, // Nivel 2 conecta con nivel 3
            {3, 4}, // Y así sucesivamente...
            {4, 5},
            {5, 6},
            {6, 7}
        };
        vp = new FitViewport(320, 160, camera);
        mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load("mundo/mapaCompleto.tmx"); // ¡Usa el nombre de tu archivo!
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        // 1. Carga la textura del jugador
        playerTexture = new Texture("mundo/marioMap.png"); // Asegúrate que el archivo esté en 'assets'
        playerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // 2. Crea la animación con la versión normal y la volteada (flip)
        TextureRegion frame1 = new TextureRegion(playerTexture);
        TextureRegion frame2 = new TextureRegion(playerTexture);
        frame2.flip(true, false); // Voltea el frame horizontalmente

        // Crea la animación que cambia de frame cada 0.5 segundos
        playerAnimation = new Animation<TextureRegion>(0.5f, frame1, frame2);

        MapObjects objects = tiledMap.getLayers().get("hitboxes").getObjects();
        MapObject startObject = objects.get("start");

        if (startObject != null) {
            // Obtenemos las coordenadas en píxeles
            float startX = startObject.getProperties().get("x", Float.class);
            float startY = startObject.getProperties().get("y", Float.class);

            // Convertimos las coordenadas de píxeles a coordenadas de grid
            playerGridX = (int) (startX / TILE_SIZE);
            playerGridY = (int) (startY / TILE_SIZE);
        } else {
            // Si no se encuentra, ponlo en una posición por defecto
            playerGridX = 1;
            playerGridY = 1;
        }

        TiledMapTileLayer caminosLayer = (TiledMapTileLayer) tiledMap.getLayers().get("caminos");
        int mapWidthInTiles = caminosLayer.getWidth();
        int mapHeightInTiles = caminosLayer.getHeight();
        esCamino = new boolean[mapWidthInTiles][mapHeightInTiles];

        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {
                // Si hay una celda en la capa "Caminos" en esta coordenada, es caminable
                esCamino[x][y] = (caminosLayer.getCell(x, y) != null);
            }
        }
        playerVisualPosition.set(playerGridX * TILE_SIZE, playerGridY * TILE_SIZE);

    }

    private void crearMapa() {
        // Crear los 7 niveles en posiciones específicas tipo Mario World

        niveles[0] = new Nivel(1, "Inicio", 100f, 200f);
        niveles[1] = new Nivel(2, "Básico", 200f, 250f);
        niveles[2] = new Nivel(3, "Esquinas", 300f, 200f);
        niveles[3] = new Nivel(4, "Laberinto", 400f, 300f);
        niveles[4] = new Nivel(5, "Complejo", 500f, 250f);
        niveles[5] = new Nivel(6, "Desafío", 600f, 200f);
        niveles[6] = new Nivel(7, "FINAL", 700f, 300f);

        for (int i = 0; i < niveles.length; i++) {
            int nivelId = niveles[i].getId();
            if (GestorProgreso.getInstancia().isNivelDesbloqueado(nivelId)) {
                if (GestorProgreso.getInstancia().isNivelCompletado(nivelId)) {
                    niveles[i].setEstado(Nivel.COMPLETADO);
                } else {
                    niveles[i].setEstado(Nivel.DISPONIBLE);
                }
            } else {
                niveles[i].setEstado(Nivel.BLOQUEADO);
            }
        }

        // Marcar el primer nivel como seleccionado
        niveles[0].setEstado(Nivel.SELECCIONADO);
    }

    private void cargarProgreso() {
        // Aquí podrías cargar desde archivo o base de datos
        // Por ahora, simularemos algunos niveles completados para prueba

        // Aplicar progreso a los niveles
        for (int i = 0; i < niveles.length; i++) {
            int nivelId = niveles[i].getId();

            if (nivelesCompletados[nivelId]) {
                niveles[i].completar();
                // Desbloquear siguiente nivel
                if (i + 1 < niveles.length) {
                    if (niveles[i + 1].getEstado() == Nivel.BLOQUEADO) {
                        niveles[i + 1].setEstado(Nivel.DISPONIBLE);
                    }
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        manejarInput();
        vp.apply();

        stateTime += delta;

        // --- LÓGICA DE ANIMACIÓN DE MOVIMIENTO ---
        if (pS == PlayerState.MOVING) {
            moveTimer += delta;
            float progress = Math.min(1f, moveTimer / MOVE_DURATION);

            // Interpolar (mover suavemente) la posición visual desde el origen al destino
            playerVisualPosition.set(moveFrom).lerp(moveTo, progress);

            // Si la animación ha terminado, volver al estado IDLE
            if (progress >= 1f) {
                pS = PlayerState.IDLE;
            }
        }
        // Limpiar pantalla con color azul oscuro tipo Mario World
        Gdx.gl.glClearColor(0.1f, 0.3f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // --- DIBUJAR EL MAPA ---
        // 1. Configura el renderizador para que use tu cámara
        mapRenderer.setView(camera);
        // 2. Dibuja el mapa en la pantalla
        mapRenderer.render();

        // AHORA, puedes dibujar cosas ENCIMA del mapa, como al jugador o la UI
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        TextureRegion currentFrame = playerAnimation.getKeyFrame(stateTime, true); // 'true' para que la animación se repita

        // Calcula dónde dibujar en píxeles, basándose en la posición del grid
        // Dibuja el frame actual en la posición calculada
        batch.draw(currentFrame, playerVisualPosition.x, playerVisualPosition.y);

        // Aquí es donde más tarde dibujarás el sprite de Mario
        // font.draw(batch, "¡Mi mapa!", 10, 10); // Ejemplo de texto
        batch.end();

    }

    private void manejarInput() {
        // Solo aceptar input de movimiento si el jugador está quieto
        if (pS == PlayerState.IDLE) {
            int targetGridX = playerGridX;
            int targetGridY = playerGridY;

            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                targetGridX++;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                targetGridX--;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                targetGridY--; // Correcto para SUBIR
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                targetGridY++; // Correcto para BAJAR
            }

            // Comprobar si la nueva posición es válida
            if (targetGridX >= 0 && targetGridX < esCamino.length
                    && targetGridY >= 0 && targetGridY < esCamino[0].length
                    && esCamino[targetGridX][targetGridY]) {

                // Si es válida, iniciar el movimiento
                iniciarMovimiento(targetGridX, targetGridY);
            }
        }
        // Aquí puedes añadir la lógica para presionar ENTER, etc.
    }

// Nuevo método de ayuda para configurar la animación
    private void iniciarMovimiento(int targetX, int targetY) {
        // Origen del movimiento (posición actual en píxeles)
        moveFrom.set(playerGridX * TILE_SIZE, playerGridY * TILE_SIZE);

        // Destino del movimiento (nueva posición en píxeles)
        moveTo.set(targetX * TILE_SIZE, targetY * TILE_SIZE);

        // Actualizar la posición lógica final del jugador
        playerGridX = targetX;
        playerGridY = targetY;

        // Iniciar la animación
        moveTimer = 0f;
        pS = PlayerState.MOVING;
    }

    private void moverSeleccion(int direccion) {
        int nuevoIndex = nivelSeleccionadoIndex;

        do {
            nuevoIndex += direccion;
            if (nuevoIndex < 0) {
                nuevoIndex = niveles.length - 1;
            }
            if (nuevoIndex >= niveles.length) {
                nuevoIndex = 0;
            }
        } while (!niveles[nuevoIndex].puedeJugar() && nuevoIndex != nivelSeleccionadoIndex);

        if (niveles[nuevoIndex].puedeJugar()) {
            seleccionarNivel(nuevoIndex);
        }
    }

    private void buscarNivelEnDireccion(float deltaX, float deltaY) {
        if (nivelSeleccionadoIndex >= niveles.length) {
            return;
        }

        Nivel nivelActual = niveles[nivelSeleccionadoIndex];
        int mejorIndex = -1;
        float menorDistancia = Float.MAX_VALUE;

        for (int i = 0; i < niveles.length; i++) {
            if (i == nivelSeleccionadoIndex || !niveles[i].puedeJugar()) {
                continue;
            }

            Nivel nivel = niveles[i];
            float dx = nivel.getX() - nivelActual.getX();
            float dy = nivel.getY() - nivelActual.getY();

            // Verificar si está en la dirección correcta
            if (deltaX > 0 && dx <= 0) {
                continue; // Buscando derecha pero nivel está a la izquierda
            }
            if (deltaX < 0 && dx >= 0) {
                continue; // Buscando izquierda pero nivel está a la derecha
            }
            if (deltaY > 0 && dy <= 0) {
                continue; // Buscando arriba pero nivel está abajo
            }
            if (deltaY < 0 && dy >= 0) {
                continue; // Buscando abajo pero nivel está arriba
            }
            float distancia = calcularDistancia(nivelActual.getX(), nivelActual.getY(),
                    nivel.getX(), nivel.getY());

            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                mejorIndex = i;
            }
        }

        if (mejorIndex != -1) {
            seleccionarNivel(mejorIndex);
        }
    }

    private void seleccionarNivel(int index) {
        // Restaurar estado del nivel previamente seleccionado
        if (nivelSeleccionadoIndex < niveles.length) {
            Nivel nivelAnterior = niveles[nivelSeleccionadoIndex];
            if (nivelAnterior.getEstado() == Nivel.SELECCIONADO) {
                int nuevoEstado = nivelesCompletados[nivelAnterior.getId()]
                        ? Nivel.COMPLETADO : Nivel.DISPONIBLE;
                nivelAnterior.setEstado(nuevoEstado);
            }
        }

        // Seleccionar nuevo nivel
        nivelSeleccionadoIndex = index;
        niveles[index].setEstado(Nivel.SELECCIONADO);
    }

    private void iniciarNivel(int nivelId) {
        System.out.println("Iniciando nivel: " + nivelId);

        // this.setScreen(new GameScreenNiveles(game, nivelId));
    }

    public void marcarNivelCompletado(int nivelId) {
        nivelesCompletados[nivelId] = true;

        // Actualizar estado del nivel
        for (int i = 0; i < niveles.length; i++) {
            if (niveles[i].getId() == nivelId) {
                niveles[i].completar();

                // Desbloquear siguiente nivel
                if (i + 1 < niveles.length
                        && niveles[i + 1].getEstado() == Nivel.BLOQUEADO) {
                    niveles[i + 1].setEstado(Nivel.DISPONIBLE);
                }
                break;
            }
        }

        // Guardar progreso
        guardarProgreso();
    }

    private void guardarProgreso() {
        // Aquí guardarías en archivo o base de datos
        System.out.println("Progreso guardado");
    }

    private void dibujarCamino(float x1, float y1, float x2, float y2, float grosor) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float longitud = (float) Math.sqrt(dx * dx + dy * dy);

        if (longitud == 0) {
            return;
        }

        dx /= longitud;
        dy /= longitud;

        float perpX = -dy * grosor / 2;
        float perpY = dx * grosor / 2;

        // Dibujar rectángulo como línea gruesa usando triángulos
        shapeRenderer.triangle(x1 + perpX, y1 + perpY, x1 - perpX, y1 - perpY, x2 + perpX, y2 + perpY);
        shapeRenderer.triangle(x2 + perpX, y2 + perpY, x2 - perpX, y2 - perpY, x1 - perpX, y1 - perpY);
    }

    private float calcularDistancia(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        
      
        vp.update(width, height, true);
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

        shapeRenderer.dispose();
        font.dispose();
        tiledMap.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        if (fondoMapa != null) {
            fondoMapa.dispose();
        }
        if (iconoCaja != null) {
            iconoCaja.dispose();
        }
        if (iconoMeta != null) {
            iconoMeta.dispose();
        }
    }
}
