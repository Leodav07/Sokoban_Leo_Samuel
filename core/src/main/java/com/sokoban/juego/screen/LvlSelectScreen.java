/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.Nivel;
import com.sokoban.juego.logica.SoundManager;
import com.sokoban.juego.logica.accounts.GestorProgreso;
import com.sokoban.juego.niveles.NivelCincoScreen;
import com.sokoban.juego.niveles.NivelDosScreen;
import com.sokoban.juego.niveles.NivelCuatroScreen;
import com.sokoban.juego.niveles.NivelSeisScreen;
import com.sokoban.juego.niveles.NivelSieteScreen;
import com.sokoban.juego.niveles.NivelTresScreen;
import com.sokoban.juego.niveles.NivelUnoScreen;

/**
 *
 * @author unwir
 */
public class LvlSelectScreen implements Screen, InputProcessor {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private BitmapFont font;

    private boolean mostrandoDialog = false;
    private String mensajeDialog = "";
    private float tiempoDialog = 0f;
    private final float DURACION_DIALOG = 3f;
    private BitmapFont dialogFont;
    private GestorProgreso gestorProgreso;
    private SpriteBatch dialogBatch;
    private Texture cuadroTexture;
    private GlyphLayout dialogLayout;
    private OrthographicCamera dialogCamera;
    private ScreenViewport dialogViewport;
    private float dialogAlpha;
    private float dialogScale;

    private enum PlayerState {
        IDLE, MOVING, ENTERING_LEVEL
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
    private TextureRegion playerHandUpFrame;
    private float transitionTimer = 0f;
    private int nivelParaCargar = -1;

    // Posición LÓGICA del jugador en el grid (casillas)
    private int playerGridX;
    private int playerGridY;

    private FrameBuffer fbo;
    private ShaderProgram wipeShader;

    private boolean moverArriba, moverAbajo, moverIzquierda, moverDerecha;

    // --- VARIABLES PARA LA ANIMACIÓN DEL MOVIMIENTO ---
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

    private Music backgroundMusic;
    private Sound moveSound;
    private Sound selectSound;

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
        SoundManager.getInstance().stopMusic();
        gestorProgreso = GestorProgreso.getInstancia();

        dialogFont = new BitmapFont();
        dialogFont.setColor(Color.RED);
        dialogFont.getData().setScale(1.5f);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false, 320, 192);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);

        camera.update();

        dialogBatch = new SpriteBatch();
        cuadroTexture = new Texture("skin/cuadro.png");
        cuadroTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Dialog font (reuse existing font or create new one)
        dialogFont = new BitmapFont();
        dialogFont.setColor(Color.BLACK);
        dialogFont.getData().setScale(1.0f);

        dialogLayout = new GlyphLayout();

        // Dialog viewport for proper scaling
        dialogCamera = new OrthographicCamera();
        dialogViewport = new ScreenViewport(dialogCamera);

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
        vp = new FitViewport(320, 192, camera);
        mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load("mundo/mapaCompleto.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        // 1. Carga la textura del jugador
        playerTexture = new Texture("mundo/marioMap.png");
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

        Gdx.input.setInputProcessor(this);

        // Crear FrameBuffer con las dimensiones correctas
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 320, 192, false);

        // Carga el shader. El vertex shader puede ser el por defecto de LibGDX.
        wipeShader = new ShaderProgram(Gdx.files.internal("default.vert"), Gdx.files.internal("wipe.frag"));
        if (!wipeShader.isCompiled()) {
            Gdx.app.error("Shader Error", wipeShader.getLog());
        }

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("musica/mapaTema.ogg"));
        // Carga los efectos de sonido
        moveSound = Gdx.audio.newSound(Gdx.files.internal("musica/smb3_map_travel.wav"));
        selectSound = Gdx.audio.newSound(Gdx.files.internal("musica/smb3_enter_level.wav"));

        backgroundMusic.setLooping(true); // Para que se repita
        backgroundMusic.setVolume(0.5f);  // Ajusta el volumen (0.0 a 1.0)
        backgroundMusic.play();
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
        // Actualizar lógica del juego
        actualizarJuego(delta);

        // Si estamos en transición, renderizar con shader
        if (pS == PlayerState.ENTERING_LEVEL) {
            renderizarConTransicion(delta);
        } else {
            renderizarNormal();
        }

        // Renderizar dialog después del contenido principal
        if (mostrandoDialog) {
            renderDialog();
        }
    }

    private void renderizarNormal() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        vp.apply();

        // Renderizar mapa
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Renderizar jugador
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        TextureRegion frameParaDibujar = playerAnimation.getKeyFrame(stateTime, true);
        batch.draw(frameParaDibujar, playerVisualPosition.x, playerVisualPosition.y);

        batch.end();
    }

    private void renderDialog() {
        dialogViewport.apply();

        // Usar las dimensiones del viewport del diálogo para cálculos
        float screenWidth = dialogViewport.getScreenWidth();
        float screenHeight = dialogViewport.getScreenHeight();

        // Preparar texto primero para medir sus dimensiones
        dialogFont.getData().setScale(dialogScale * 0.8f);
        dialogLayout.setText(dialogFont, mensajeDialog);

        // Calcular tamaño del diálogo basado en el texto + padding
        float textWidth = dialogLayout.width;
        float textHeight = dialogLayout.height;

        // Padding alrededor del texto
        float paddingX = 40 * dialogScale;
        float paddingY = 30 * dialogScale;

        // Tamaño mínimo y máximo para el diálogo
        float minWidth = 200 * dialogScale;
        float maxWidth = Math.min(screenWidth * 0.8f, 600 * dialogScale);
        float minHeight = 80 * dialogScale;
        float maxHeight = Math.min(screenHeight * 0.6f, 300 * dialogScale);

        // Calcular dimensiones finales del diálogo
        float dialogWidth = Math.max(minWidth, Math.min(maxWidth, textWidth + paddingX * 2));
        float dialogHeight = Math.max(minHeight, Math.min(maxHeight, textHeight + paddingY * 2));

        // Si el texto es muy largo, ajustar para texto multi-línea
        if (textWidth + paddingX * 2 > maxWidth) {
            // Recalcular el texto con wrap para que quepa en el ancho máximo
            float wrapWidth = maxWidth - paddingX * 2;
            dialogLayout.setText(dialogFont, mensajeDialog, Color.BLACK, wrapWidth, 1, true);
            dialogHeight = Math.max(minHeight, dialogLayout.height + paddingY * 2);
        }

        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;

        dialogCamera.update();
        dialogBatch.setProjectionMatrix(dialogCamera.combined);
        dialogBatch.begin();

        // Aplicar transparencia
        dialogBatch.setColor(1, 1, 1, dialogAlpha);

        // Dibujar el cuadro de fondo con el tamaño ajustado
        dialogBatch.draw(cuadroTexture, dialogX, dialogY, dialogWidth, dialogHeight);

        // Calcular posición del texto centrada
        float textX = dialogX + (dialogWidth - dialogLayout.width) / 2;
        float textY = dialogY + (dialogHeight + dialogLayout.height) / 2;

        // Configurar fuente con transparencia
        dialogFont.setColor(0.1f, 0.1f, 0.1f, dialogAlpha); // Texto negro semi-transparente

        // Dibujar el texto (ya tiene el wrap aplicado si es necesario)
        dialogFont.draw(dialogBatch, dialogLayout, textX, textY);

        // Restaurar configuración de la fuente
        dialogFont.getData().setScale(1f);
        dialogFont.setColor(Color.BLACK);

        // Restaurar color del batch
        dialogBatch.setColor(Color.WHITE);

        dialogBatch.end();
    }

    private void renderizarConTransicion(float delta) {
        transitionTimer += delta;

        // --- FASE 1: RENDERIZAR ESCENA EN FRAMEBUFFER ---
        fbo.begin();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        TextureRegion frameParaDibujar = playerAnimation.getKeyFrame(stateTime, true);
        batch.draw(frameParaDibujar, playerVisualPosition.x, playerVisualPosition.y);

        batch.end();
        fbo.end();

        // --- FASE 2: RENDERIZAR FRAMEBUFFER CON SHADER ---
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        vp.apply();
        batch.setProjectionMatrix(vp.getCamera().combined);
        batch.begin();

        // Aplicar shader de transición Mario Bros 3
        batch.setShader(wipeShader);

        // Calcular progreso del wipe (0.0 a 1.0)
        float progress = Math.min((transitionTimer - 0.5f) / 1.0f, 1.0f);
        if (progress < 0) {
            progress = 0;
        }

        // Pasar parámetros al shader
        wipeShader.setUniformf("u_progress", progress);
        wipeShader.setUniformf("u_resolution", camera.viewportWidth, camera.viewportHeight);

        // Dibujar textura del FBO SIN INVERTIR (usar coordenadas normales)
        Texture fboTexture = fbo.getColorBufferTexture();
        TextureRegion fboRegion = new TextureRegion(fboTexture);
        fboRegion.flip(false, true); // Solo voltear Y para corregir el FrameBuffer

        batch.draw(fboRegion, 0, 0, camera.viewportWidth, camera.viewportHeight);

        batch.end();
        batch.setShader(null); // ¡Importante! Restaurar shader por defecto

        // Cambiar de pantalla cuando termine la transición
        if (transitionTimer > 1.5f) {
            System.out.println("CAMBIANDO DE PANTALLA A NIVEL " + nivelParaCargar);
            switch (nivelParaCargar) {
                case 1:
                    game.setScreen(new NivelUnoScreen(game));
                    break;
                case 2:
                    game.setScreen(new NivelDosScreen(game));
                    break;
                case 3:
                    game.setScreen(new NivelTresScreen(game));
                    break;
                case 4:
                    game.setScreen(new NivelCuatroScreen(game));
                    break;
                case 5:
                    game.setScreen(new NivelCincoScreen(game));
                    break;

                case 6:
                    game.setScreen(new NivelSeisScreen(game));
                    break;

                case 7:

                    game.setScreen(new NivelSieteScreen(game));
                    break;

            }
        }
    }

    private void manejarInput() {
        if (pS == PlayerState.IDLE) {
            int targetGridX = playerGridX;
            int targetGridY = playerGridY;

            if (moverDerecha) {
                targetGridX++;
            } else if (moverIzquierda) {
                targetGridX--;
            } else if (moverArriba) {
                targetGridY++; // Eje Y corregido
            } else if (moverAbajo) {
                targetGridY--; // Eje Y corregido
            }

            // Si se intentó mover y el movimiento es válido
            if ((moverDerecha || moverIzquierda || moverArriba || moverAbajo)
                    && esMovimientoValido(targetGridX, targetGridY)) {

                // Consumimos el input para que no se mueva sin parar
                moverDerecha = moverIzquierda = moverArriba = moverAbajo = false;
                iniciarMovimiento(targetGridX, targetGridY);
            }
        }
    }

    private void iniciarMovimiento(int targetX, int targetY) {
        // Origen del movimiento (posición actual en píxeles)
        moveFrom.set(playerVisualPosition);

        // Destino del movimiento (nueva posición en píxeles)
        moveTo.set(targetX * TILE_SIZE, targetY * TILE_SIZE);

        // Actualizar la posición lógica final del jugador
        playerGridX = targetX;
        playerGridY = targetY;

        // Iniciar la animación
        moveTimer = 0f;
        pS = PlayerState.MOVING;
        moveSound.play(1.0f);
    }

    private void seleccionarNivelActual() {
        // Solo podemos seleccionar un nivel si no nos estamos moviendo y no hay dialog activo
        if (pS != PlayerState.IDLE || mostrandoDialog) {
            return;
        }

        // Obtenemos todos los objetos de la capa
        MapObjects objects = tiledMap.getLayers().get("hitboxes").getObjects();

        // Recorremos cada objeto para ver si coincide con la posición del jugador
        for (MapObject object : objects) {
            // Obtenemos las coordenadas del objeto en píxeles
            float objX = object.getProperties().get("x", Float.class);
            float objY = object.getProperties().get("y", Float.class);

            // Convertimos esas coordenadas a la cuadrícula (grid)
            int objGridX = (int) (objX / TILE_SIZE);
            int objGridY = (int) (objY / TILE_SIZE);

            // Si la posición del jugador coincide con la posición de este objeto...
            if (playerGridX == objGridX && playerGridY == objGridY) {

                // ...y el nombre del objeto empieza con "nivel_"
                String objectName = object.getName();
                if (objectName != null && objectName.startsWith("nivel_")) {

                    try {
                        // Extraemos el número del nombre (ej: "nivel_1" -> 1)
                        String numeroNivelStr = objectName.substring("nivel_".length());
                        int nivelId = Integer.parseInt(numeroNivelStr);

                        // Verificar si el nivel está desbloqueado
                        if (nivelId == 1 || gestorProgreso.isNivelDesbloqueado(nivelId)) {
                            // Nivel desbloqueado, permitir entrada
                            iniciarNivel(nivelId);
                        } else {
                            // Nivel bloqueado, mostrar dialog
                            mostrarDialogNivelBloqueado(nivelId);
                        }
                        return;

                    } catch (NumberFormatException e) {
                        // El nombre del objeto no tenía un número válido después de "nivel_"
                        System.out.println("Advertencia: Objeto mal nombrado: " + objectName);
                    }
                }
            }
        }
    }

// Add this new method to show the blocked level dialog
    private void mostrarDialogNivelBloqueado(int nivelId) {
        mensajeDialog = "¡Nivel " + nivelId + " bloqueado!\nCompleta el nivel anterior para desbloquearlo";
        mostrarDialog(mensajeDialog);
    }

    private void mostrarDialog(String mensaje) {
        this.mensajeDialog = mensaje;
        this.mostrandoDialog = true;
        this.dialogAlpha = 0f;
        this.dialogScale = 0.5f;

        animateDialog(true);
    }

    private void ocultarDialog() {
        animateDialog(false);
    }

    private void animateDialog(boolean show) {
        if (show) {
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                float time = 0f;

                @Override
                public void run() {
                    time += 0.02f;
                    if (time >= 0.3f) {
                        dialogAlpha = 1f;
                        dialogScale = 1f;
                        this.cancel();
                        return;
                    }

                    dialogAlpha = Interpolation.pow2Out.apply(time / 0.3f);
                    dialogScale = 0.5f + (0.5f * Interpolation.bounceOut.apply(time / 0.3f));
                }
            }, 0f, 0.02f);
        } else {
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                float time = 0f;

                @Override
                public void run() {
                    time += 0.02f;
                    if (time >= 0.2f) {
                        mostrandoDialog = false;
                        mensajeDialog = "";
                        this.cancel();
                        return;
                    }

                    dialogAlpha = 1f - (time / 0.2f);
                    dialogScale = 1f - (0.3f * (time / 0.2f));
                }
            }, 0f, 0.02f);
        }
    }

// Add this to your actualizarJuego() method, after the existing movement animation code
    private void actualizarJuego(float delta) {
        manejarInput();
        stateTime += delta;

        // No need for manual dialog timer - it's handled by user input now
        // Actualizar animación de movimiento
        if (pS == PlayerState.MOVING) {
            moveTimer += delta;
            float progress = Math.min(moveTimer / MOVE_DURATION, 1.0f);

            // Interpolación suave del movimiento
            playerVisualPosition.x = Interpolation.smooth.apply(moveFrom.x, moveTo.x, progress);
            playerVisualPosition.y = Interpolation.smooth.apply(moveFrom.y, moveTo.y, progress);

            // Si la animación terminó
            if (progress >= 1.0f) {
                pS = PlayerState.IDLE;
                playerVisualPosition.set(moveTo);
            }
        }
    }

// Add this method to render the dialog in your renderizarNormal() method
// Add this new method to render the dialog
// Add this to your keyDown method to allow dismissing the dialog with ENTER or ESCAPE
    public boolean keyDown(int keycode) {
        // Si hay un dialog activo, permitir cerrarlo con ENTER o ESCAPE
        if (mostrandoDialog) {
            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.ESCAPE) {
                ocultarDialog();
                return true;
            }
            return true; // Consume all input while dialog is active
        }

        // Existing movement code...
        switch (keycode) {
            case Input.Keys.UP:
                moverArriba = true;
                break;
            case Input.Keys.DOWN:
                moverAbajo = true;
                break;
            case Input.Keys.LEFT:
                moverIzquierda = true;
                break;
            case Input.Keys.RIGHT:
                moverDerecha = true;
                break;
        }

        if (keycode == Input.Keys.ENTER) {
            seleccionarNivelActual();
        }
        return true;
    }

    private void iniciarNivel(int nivelId) {
        System.out.println("Iniciando nivel: " + nivelId);

        if (pS == PlayerState.ENTERING_LEVEL) {
            return;
        }

        pS = PlayerState.ENTERING_LEVEL; // Cambiamos al nuevo estado
        transitionTimer = 0f;             // Reiniciamos el temporizador
        nivelParaCargar = nivelId;
        selectSound.play(1.0f);
        backgroundMusic.stop();

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


    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        vp.update(width, height, true);

        // Update dialog viewport
        if (dialogViewport != null) {
            dialogViewport.update(width, height, true);
        }

        // Recrear FrameBuffer with new dimensions if necessary
        if (fbo != null) {
            fbo.dispose();
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 320, 192, false);
            fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
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
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.UP:
                moverArriba = false;
                break;
            case Input.Keys.DOWN:
                moverAbajo = false;
                break;
            case Input.Keys.LEFT:
                moverIzquierda = false;
                break;
            case Input.Keys.RIGHT:
                moverDerecha = false;
                break;
        }
        return true;
    }

    private boolean esMovimientoValido(int targetX, int targetY) {
        // Primero, comprueba que las coordenadas no se salen de los límites del array
        if (targetX < 0 || targetX >= esCamino.length
                || targetY < 0 || targetY >= esCamino[0].length) {
            return false; // Está fuera del mapa
        }

        // Si está dentro del mapa, comprueba si esa casilla es un camino
        return esCamino[targetX][targetY];
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (playerTexture != null) {
            playerTexture.dispose();
        }
        if (fbo != null) {
            fbo.dispose();
        }
        if (wipeShader != null) {
            wipeShader.dispose();
        }
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
