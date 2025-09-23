package com.sokoban.juego.screen;

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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.Nivel;
import com.sokoban.juego.logica.SoundManager;
import com.sokoban.juego.logica.accounts.GestorProgreso;
import com.sokoban.juego.niveles.*;

public class LvlSelectScreen implements Screen, InputProcessor {

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private BitmapFont font; // <<-- Fuente para las instrucciones
    private Skin skin;

    // --- Variables del diálogo personalizado (se podrían refactorizar en el futuro) ---
    private boolean mostrandoDialog = false;
    private String mensajeDialog = "";
    private SpriteBatch dialogBatch;
    private Texture cuadroTexture;
    private GlyphLayout dialogLayout;
    private OrthographicCamera dialogCamera;
    private ScreenViewport dialogViewport;
    private float dialogAlpha;
    private float dialogScale;

    // --- Resto de variables de la clase (sin cambios) ---
    private enum PlayerState {
        IDLE, MOVING, ENTERING_LEVEL
    }
    private PlayerState pS = PlayerState.IDLE;
    private Nivel[] niveles;
    private int nivelSeleccionadoIndex;
    private Texture playerTexture;
    private Animation<TextureRegion> playerAnimation;
    private float stateTime = 0f;
    private float transitionTimer = 0f;
    private int nivelParaCargar = -1;
    private int playerGridX, playerGridY;
    private FrameBuffer fbo;
    private ShaderProgram wipeShader;
    private boolean moverArriba, moverAbajo, moverIzquierda, moverDerecha;
    private Vector2 playerVisualPosition = new Vector2();
    private Vector2 moveFrom = new Vector2();
    private Vector2 moveTo = new Vector2();
    private float moveTimer = 0f;
    private final float MOVE_DURATION = 0.2f;
    private Main game;
    private Music backgroundMusic;
    private Sound moveSound;
    private Sound selectSound;
    private Viewport vp;
    private boolean[][] esCamino;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final int TILE_SIZE = 16;
    private GestorProgreso gestorProgreso;

    public LvlSelectScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        inicializar();
        crearMapa();
        cargarProgreso();
    }

    private void inicializar() {
        SoundManager.getInstance().stopMusic();
        gestorProgreso = GestorProgreso.getInstancia();
        batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false, 320, 192);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();

        // <<-- CORRECCIÓN: Se carga el skin y se obtiene la fuente UNA SOLA VEZ -->>
        try {
            TextureAtlas atlas = new TextureAtlas("mario.atlas");
            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);
            font = skin.getFont("default-font"); // Se obtiene la fuente correcta del skin
        } catch (Exception e) {
            Gdx.app.error("LvlSelectScreen", "Error cargando skin, usando fuente por defecto.", e);
            skin = new Skin();
            font = new BitmapFont(); // Fallback en caso de error
        }

        // El resto de la inicialización...
        dialogBatch = new SpriteBatch();
        cuadroTexture = new Texture("skin/cuadro.png");
        cuadroTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        dialogLayout = new GlyphLayout();
        dialogCamera = new OrthographicCamera();
        dialogViewport = new ScreenViewport(dialogCamera);
        niveles = new Nivel[7];
        nivelSeleccionadoIndex = 0;
        vp = new FitViewport(320, 192, camera);
        tiledMap = new TmxMapLoader().load("mundo/mapaCompleto.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        playerTexture = new Texture("mundo/marioMap.png");
        playerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion frame1 = new TextureRegion(playerTexture);
        TextureRegion frame2 = new TextureRegion(playerTexture);
        frame2.flip(true, false);
        playerAnimation = new Animation<>(0.5f, frame1, frame2);
        MapObjects objects = tiledMap.getLayers().get("hitboxes").getObjects();
        MapObject startObject = objects.get("start");
        if (startObject != null) {
            float startX = startObject.getProperties().get("x", Float.class);
            float startY = startObject.getProperties().get("y", Float.class);
            playerGridX = (int) (startX / TILE_SIZE);
            playerGridY = (int) (startY / TILE_SIZE);
        } else {
            playerGridX = 1;
            playerGridY = 1;
        }
        TiledMapTileLayer caminosLayer = (TiledMapTileLayer) tiledMap.getLayers().get("caminos");
        esCamino = new boolean[caminosLayer.getWidth()][caminosLayer.getHeight()];
        for (int x = 0; x < caminosLayer.getWidth(); x++) {
            for (int y = 0; y < caminosLayer.getHeight(); y++) {
                esCamino[x][y] = (caminosLayer.getCell(x, y) != null);
            }
        }
        playerVisualPosition.set(playerGridX * TILE_SIZE, playerGridY * TILE_SIZE);
        Gdx.input.setInputProcessor(this);
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 320, 192, false);
        wipeShader = new ShaderProgram(Gdx.files.internal("default.vert"), Gdx.files.internal("wipe.frag"));
        if (!wipeShader.isCompiled()) {
            Gdx.app.error("Shader Error", wipeShader.getLog());
        }
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("musica/mapaTema.ogg"));
        moveSound = Gdx.audio.newSound(Gdx.files.internal("musica/smb3_map_travel.wav"));
        selectSound = Gdx.audio.newSound(Gdx.files.internal("musica/smb3_enter_level.wav"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();
    }

    // ... crearMapa y cargarProgreso sin cambios ...
    private void crearMapa() {
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
                niveles[i].setEstado(GestorProgreso.getInstancia().isNivelCompletado(nivelId) ? Nivel.COMPLETADO : Nivel.DISPONIBLE);
            } else {
                niveles[i].setEstado(Nivel.BLOQUEADO);
            }
        }
        niveles[0].setEstado(Nivel.SELECCIONADO);
    }

    private void cargarProgreso() {
        // Esta lógica parece redundante si ya se establece en crearMapa, pero la mantenemos por si acaso.
    }

    @Override
    public void render(float delta) {
        actualizarJuego(delta);
        if (pS == PlayerState.ENTERING_LEVEL) {
            renderizarConTransicion(delta);
        } else {
            renderizarNormal();
        }
        if (mostrandoDialog) {
            renderDialog();
        }
    }

    private void renderizarNormal() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        vp.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(playerAnimation.getKeyFrame(stateTime, true), playerVisualPosition.x, playerVisualPosition.y);

        // <<-- CORRECCIÓN: Ahora usa la fuente correcta del skin -->>
        String instructions = "ENTER: Seleccionar | BACKSPACE: Volver";
        font.getData().setScale(0.5f); // Aplicamos la escala deseada
        font.draw(batch, instructions, 5, 15);
        font.getData().setScale(1.0f); // Restauramos la escala por si se usa en otro lado

        batch.end();
    }

    // ... El resto del archivo permanece igual (incluyendo el keyDown y dispose corregidos previamente) ...
    private void renderDialog() {
        dialogViewport.apply();
        float screenWidth = dialogViewport.getScreenWidth();
        float screenHeight = dialogViewport.getScreenHeight();
        BitmapFont dialogFont = skin.getFont("default-font"); // Usar la fuente del skin también aquí
        dialogFont.getData().setScale(dialogScale * 0.8f);
        dialogLayout.setText(dialogFont, mensajeDialog);
        float textWidth = dialogLayout.width;
        float paddingX = 40 * dialogScale;
        float maxWidth = Math.min(screenWidth * 0.8f, 600 * dialogScale);

        float dialogWidth = Math.max(200 * dialogScale, Math.min(maxWidth, textWidth + paddingX * 2));
        float dialogHeight;

        if (textWidth + paddingX * 2 > maxWidth) {
            float wrapWidth = maxWidth - paddingX * 2;
            dialogLayout.setText(dialogFont, mensajeDialog, Color.BLACK, wrapWidth, 1, true);
            dialogHeight = Math.max(80 * dialogScale, dialogLayout.height + (30 * dialogScale) * 2);
        } else {
            dialogHeight = Math.max(80 * dialogScale, dialogLayout.height + (30 * dialogScale) * 2);
        }

        float dialogX = (screenWidth - dialogWidth) / 2;
        float dialogY = (screenHeight - dialogHeight) / 2;

        dialogCamera.update();
        dialogBatch.setProjectionMatrix(dialogCamera.combined);
        dialogBatch.begin();
        dialogBatch.setColor(1, 1, 1, dialogAlpha);
        dialogBatch.draw(cuadroTexture, dialogX, dialogY, dialogWidth, dialogHeight);
        float textX = dialogX + (dialogWidth - dialogLayout.width) / 2;
        float textY = dialogY + (dialogHeight + dialogLayout.height) / 2;
        dialogFont.setColor(0.1f, 0.1f, 0.1f, dialogAlpha);
        dialogFont.draw(dialogBatch, dialogLayout, textX, textY);
        dialogFont.getData().setScale(1f);
        dialogBatch.setColor(Color.WHITE);
        dialogBatch.end();
    }

    private void renderizarConTransicion(float delta) {
        transitionTimer += delta;
        fbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(playerAnimation.getKeyFrame(stateTime, true), playerVisualPosition.x, playerVisualPosition.y);
        batch.end();
        fbo.end();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        vp.apply();
        batch.setProjectionMatrix(vp.getCamera().combined);
        batch.begin();
        batch.setShader(wipeShader);
        float progress = Math.min((transitionTimer - 0.5f) / 1.0f, 1.0f);
        progress = Math.max(progress, 0);
        wipeShader.setUniformf("u_progress", progress);
        wipeShader.setUniformf("u_resolution", camera.viewportWidth, camera.viewportHeight);
        TextureRegion fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
        batch.draw(fboRegion, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.end();
        batch.setShader(null);
        if (transitionTimer > 1.5f) {
            dispose(); // Liberar recursos ANTES de cambiar de pantalla
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
                targetGridY++;
            } else if (moverAbajo) {
                targetGridY--;
            }
            if ((moverDerecha || moverIzquierda || moverArriba || moverAbajo) && esMovimientoValido(targetGridX, targetGridY)) {
                moverDerecha = moverIzquierda = moverArriba = moverAbajo = false;
                iniciarMovimiento(targetGridX, targetGridY);
            }
        }
    }

    private void iniciarMovimiento(int targetX, int targetY) {
        moveFrom.set(playerVisualPosition);
        moveTo.set(targetX * TILE_SIZE, targetY * TILE_SIZE);
        playerGridX = targetX;
        playerGridY = targetY;
        moveTimer = 0f;
        pS = PlayerState.MOVING;
        moveSound.play(1.0f);
    }

    private void seleccionarNivelActual() {
        if (pS != PlayerState.IDLE || mostrandoDialog) {
            return;
        }
        MapObjects objects = tiledMap.getLayers().get("hitboxes").getObjects();
        for (MapObject object : objects) {
            float objX = object.getProperties().get("x", Float.class);
            float objY = object.getProperties().get("y", Float.class);
            if (playerGridX == (int) (objX / TILE_SIZE) && playerGridY == (int) (objY / TILE_SIZE)) {
                String objectName = object.getName();
                if (objectName != null && objectName.startsWith("nivel_")) {
                    try {
                        int nivelId = Integer.parseInt(objectName.substring("nivel_".length()));
                        if (nivelId == 1 || gestorProgreso.isNivelDesbloqueado(nivelId)) {
                            iniciarNivel(nivelId);
                        } else {
                            mostrarDialogNivelBloqueado(nivelId);
                        }
                        return;
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
    }

    private void mostrarDialogNivelBloqueado(int nivelId) {
        mostrarDialog("¡Nivel " + nivelId + " bloqueado!\nCompleta el nivel anterior para desbloquearlo");
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

    private void actualizarJuego(float delta) {
        manejarInput();
        stateTime += delta;
        if (pS == PlayerState.MOVING) {
            moveTimer += delta;
            float progress = Math.min(moveTimer / MOVE_DURATION, 1.0f);
            playerVisualPosition.interpolate(moveTo, progress, Interpolation.smooth);
            if (progress >= 1.0f) {
                pS = PlayerState.IDLE;
                playerVisualPosition.set(moveTo);
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (mostrandoDialog) {
            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.ESCAPE) {
                ocultarDialog();
            }
            return true;
        }
        if (keycode == Input.Keys.BACKSPACE) {
            backgroundMusic.stop();
            SoundManager.getInstance().playMusic(SoundManager.MusicTrack.MENU_TEMA, true);
            game.setScreen(new CortinaTransicion(game, LvlSelectScreen.this, new MenuScreen(game)));
            return true;
        }
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
        if (pS == PlayerState.ENTERING_LEVEL) {
            return;
        }
        pS = PlayerState.ENTERING_LEVEL;
        transitionTimer = 0f;
        nivelParaCargar = nivelId;
        selectSound.play(1.0f);
        backgroundMusic.stop();
    }

    @Override
    public void resize(int width, int height) {
        vp.update(width, height, true);
        if (dialogViewport != null) {
            dialogViewport.update(width, height, true);
        }
        if (fbo != null) {
            fbo.dispose();
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 320, 192, false);
            fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (skin != null) {
            skin.dispose();
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
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        if (moveSound != null) {
            moveSound.dispose();
        }
        if (selectSound != null) {
            selectSound.dispose();
        }
        if (cuadroTexture != null) {
            cuadroTexture.dispose();
        }
        if (dialogBatch != null) {
            dialogBatch.dispose();
        }
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
        if (targetX < 0 || targetX >= esCamino.length || targetY < 0 || targetY >= esCamino[0].length) {
            return false;
        }
        return esCamino[targetX][targetY];
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
}
