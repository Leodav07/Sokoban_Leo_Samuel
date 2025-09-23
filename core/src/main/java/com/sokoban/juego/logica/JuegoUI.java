package com.sokoban.juego.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sokoban.juego.Main;
import com.sokoban.juego.niveles.ConfigNiveles;
import com.badlogic.gdx.utils.Align;

public class JuegoUI {

    private Stage uiStage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;

    // UI Elements
    private Label nivelLabel;
    private Label movimientosLabel;
    private Label tiempoLabel;
    private Label instruccionesLabel;
    private Table uiPanel;

    // Resultado del nivel
    private Stage resultStage;
    private Dialog resultDialog;

    // Variables de juego
    private int nivelActual;
    private int movimientosRealizados;
    private int movimientosPar;
    private long tiempoInicio;
    private int puntajeFinal, movimientosFinal;
    private long tiempoFinal;

    // Variables para efectos visuales
    private float tiempoAnimacion = 0f;
    private float particleTime = 0f;
    private boolean mostrandoVictoria = false;
    private Main game;
    // Colores mejorados
    private final Color COLOR_FONDO_UI = new Color(0.05f, 0.05f, 0.15f, 0.95f);
    private final Color COLOR_PANEL_BORDE = new Color(0.3f, 0.7f, 1f, 0.8f);
    private final Color COLOR_TEXTO_PRINCIPAL = new Color(0.9f, 0.9f, 1f, 1f);
    private final Color COLOR_TEXTO_DESTACADO = new Color(1f, 1f, 0.3f, 1f);
    private final Color COLOR_VICTORIA_FONDO = new Color(0, 0, 0, 0.9f);
    private final Color COLOR_VICTORIA_PANEL = new Color(0.1f, 0.1f, 0.3f, 0.98f);
    private final Color COLOR_ESTRELLAS = new Color(1f, 0.8f, 0.2f, 1f);
    private final Color COLOR_GLOW_DORADO = new Color(1f, 0.9f, 0.3f, 0.6f);

    private final int PANEL_HEIGHT = 130;
    private final int DIALOG_WIDTH = 500;
    private final int DIALOG_HEIGHT = 450;

    public JuegoUI(Main game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();
        inicializarUI();
    }

    private void inicializarUI() {
        try {
            TextureAtlas atlas = new TextureAtlas("mario.atlas");

            // Configurar filtrado nearest neighbor para evitar blur
            for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
                region.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }

            skin = new Skin(Gdx.files.internal("skin/mario_skin.json"), atlas);

            uiStage = new Stage(new FitViewport(800, 480));
            resultStage = new Stage(new FitViewport(800, 480));

            crearPanelUI();
        } catch (Exception e) {
            Gdx.app.error("JuegoUI", "Error inicializando UI con skin", e);
            // Fallback a skin básica
            skin = new Skin();
            uiStage = new Stage(new FitViewport(800, 480));
            resultStage = new Stage(new FitViewport(800, 480));
        }
    }

    private void crearPanelUI() {
        // Panel principal de UI con efectos mejorados
        uiPanel = new Table();
        uiPanel.setFillParent(false);
        uiPanel.setSize(800, PANEL_HEIGHT);
        uiPanel.setPosition(0, 0);
        uiStage.addActor(uiPanel);

        // Labels principales con colores mejorados
        nivelLabel = new Label("", skin, "lvltitle");
        movimientosLabel = new Label("", skin, "lvl");
        tiempoLabel = new Label("", skin, "lvl");
        instruccionesLabel = new Label("", skin, "lvl");

        // Aplicar colores y escalas mejoradas
        nivelLabel.setColor(COLOR_TEXTO_DESTACADO);
        nivelLabel.setFontScale(0.9f);

        movimientosLabel.setColor(COLOR_TEXTO_PRINCIPAL);
        movimientosLabel.setFontScale(0.7f);

        tiempoLabel.setColor(COLOR_TEXTO_PRINCIPAL);
        tiempoLabel.setFontScale(0.7f);

        instruccionesLabel.setColor(new Color(0.7f, 0.7f, 0.8f, 0.9f));
        instruccionesLabel.setFontScale(0.55f);

        // Layout del panel mejorado
        Table topRow = new Table();
        topRow.add(nivelLabel).expandX().left().pad(15);
        topRow.add(movimientosLabel).pad(15);
        topRow.add(tiempoLabel).pad(15);

        uiPanel.add(topRow).expandX().fillX().height(60);
        uiPanel.row();
        uiPanel.add(instruccionesLabel).expandX().left().pad(5, 15, 15, 15);

        // Animación inicial más elegante
        uiPanel.setColor(1, 1, 1, 0);
        uiPanel.setPosition(0, -PANEL_HEIGHT);
        uiPanel.addAction(Actions.parallel(
                Actions.fadeIn(0.6f, Interpolation.pow2Out),
                Actions.moveTo(0, 0, 0.5f, Interpolation.bounceOut)
        ));
    }

    public void iniciarNivel(int nivel) {
        this.nivelActual = nivel;
        this.movimientosRealizados = 0;
        this.movimientosPar = ConfigNiveles.getMovimientosObjetivo(nivel);
        this.tiempoInicio = System.currentTimeMillis();
        this.resultDialog = null;
        // Actualizar labels con mejor formato
        nivelLabel.setText(game.bundle.get("juego.nivel") + nivelActual + " - " + ConfigNiveles.getNombreNivel(nivelActual).toUpperCase());
        instruccionesLabel.setText(game.bundle.get("juego.funciones"));

        actualizarInformacion(0);

        // Animación de entrada para el nuevo nivel más llamativa
        uiPanel.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.scaleTo(0.95f, 0.95f, 0.15f, Interpolation.pow2Out),
                        Actions.color(COLOR_TEXTO_DESTACADO, 0.15f)
                ),
                Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.25f, Interpolation.elasticOut),
                        Actions.color(Color.WHITE, 0.25f)
                )
        ));
    }

    public void incrementarMovimientos() {
        movimientosRealizados++;
        actualizarInformacion(0);

        movimientosLabel.clearActions();

        movimientosLabel.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.scaleTo(1.2f, 1.2f, 0.1f, Interpolation.pow2Out),
                        Actions.color(COLOR_TEXTO_DESTACADO, 0.1f)
                ),
                Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.15f, Interpolation.bounceOut),
                        Actions.color(COLOR_TEXTO_PRINCIPAL, 0.15f)
                )
        ));
    }

    public void decrementarMovimientos() {
        if (movimientosRealizados > 0) {
            movimientosRealizados--;
            actualizarInformacion(0);

            // Animación para undo más visible
            movimientosLabel.addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.color(new Color(0.3f, 1f, 0.3f, 1f), 0.2f),
                            Actions.scaleTo(1.1f, 1.1f, 0.2f)
                    ),
                    Actions.parallel(
                            Actions.color(COLOR_TEXTO_PRINCIPAL, 0.3f),
                            Actions.scaleTo(1f, 1f, 0.3f)
                    )
            ));
        }
    }

    private void actualizarInformacion(long tiempoPausado) {
        String movText = game.bundle.get("juego.movimiento") + movimientosRealizados + " / " + movimientosPar;
        if (movimientosRealizados <= movimientosPar) {
            movimientosLabel.setColor(new Color(0.3f, 1f, 0.3f, 1f));
        } else {
            movimientosLabel.setColor(new Color(1f, 0.7f, 0.3f, 1f));
        }
        movimientosLabel.setText(movText);

        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio - tiempoPausado;
        tiempoLabel.setText(game.bundle.get("juego.tiempo") + formatearTiempo(tiempoTranscurrido));
    }

    public void setResultadoFinal(int puntaje, int movimientos, long tiempo) {
        this.puntajeFinal = puntaje;
        this.movimientosFinal = movimientos;
        this.tiempoFinal = tiempo;
        this.mostrandoVictoria = true;
        this.tiempoAnimacion = 0f;
        this.particleTime = 0f;
    }

    public void dibujar(SpriteBatch batch, int gameWorldWidth, int gameWorldHeight, long tiempoPausado) {
        // Actualizar tiempo para efectos
        tiempoAnimacion += Gdx.graphics.getDeltaTime();

        // Solo hacer end() si el batch está activo
        if (batch.isDrawing()) {
            batch.end();
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        // Fondo principal con gradiente simulado
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_FONDO_UI);
        shapeRenderer.rect(0, 0, gameWorldWidth, PANEL_HEIGHT);
        shapeRenderer.end();

        // Borde superior brillante
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float brightness = 0.6f + 0.2f * MathUtils.sin(tiempoAnimacion * 2f);
        shapeRenderer.setColor(COLOR_PANEL_BORDE.r * brightness,
                COLOR_PANEL_BORDE.g * brightness,
                COLOR_PANEL_BORDE.b * brightness,
                COLOR_PANEL_BORDE.a);
        shapeRenderer.rect(0, PANEL_HEIGHT - 3, gameWorldWidth, 3);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Asegurar que el batch esté listo para dibujar
        if (!batch.isDrawing()) {
            batch.begin();
        }

        // Actualizar información en tiempo real
        actualizarInformacion(tiempoPausado);

        // Dibujar UI
        uiStage.getViewport().apply();
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }

    public void mostrarResultadoNivel(SpriteBatch batch) {
        if (resultDialog == null) {
            SoundManager.getInstance().play(SoundManager.SoundEffect.NIVEL_COMPLETADO);
            crearDialogoResultado();
        }

        // Actualizar efectos para la pantalla de victoria
        particleTime += Gdx.graphics.getDeltaTime();

        // Solo hacer end() si el batch está activo
        if (batch.isDrawing()) {
            batch.end();
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        shapeRenderer.setProjectionMatrix(resultStage.getCamera().combined);

        // Fondo animado de victoria
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float alpha = 0.9f + 0.05f * MathUtils.sin(particleTime * 1.5f);
        shapeRenderer.setColor(COLOR_VICTORIA_FONDO.r, COLOR_VICTORIA_FONDO.g,
                COLOR_VICTORIA_FONDO.b, alpha);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        // Efectos de partículas doradas
        dibujarParticulasVictoria(screenWidth, screenHeight);

        // Glow detrás del diálogo
        dibujarGlowDialog(screenWidth, screenHeight);

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Asegurar que el batch esté listo para dibujar
        if (!batch.isDrawing()) {
            batch.begin();
        }

        // Dibujar el diálogo de resultado
        resultStage.getViewport().apply();
        resultStage.act(Gdx.graphics.getDeltaTime());
        resultStage.draw();
    }

    private void dibujarParticulasVictoria(int screenWidth, int screenHeight) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Partículas doradas flotantes
        for (int i = 0; i < 25; i++) {
            float x = (i * 47 + particleTime * 30) % screenWidth;
            float y = (i * 31 + particleTime * 25) % screenHeight;
            float size = 2f + 2f * MathUtils.sin(particleTime * 3f + i * 0.5f);
            float alpha = 0.4f + 0.3f * MathUtils.sin(particleTime * 2f + i);

            if (alpha > 0) {
                shapeRenderer.setColor(COLOR_ESTRELLAS.r, COLOR_ESTRELLAS.g,
                        COLOR_ESTRELLAS.b, alpha);
                shapeRenderer.circle(x, y, size);
            }
        }

        shapeRenderer.end();
    }

    private void dibujarGlowDialog(int screenWidth, int screenHeight) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int dialogX = (screenWidth - DIALOG_WIDTH) / 2;
        int dialogY = (screenHeight - DIALOG_HEIGHT) / 2;

        // Múltiples capas de glow dorado
        for (int i = 0; i < 4; i++) {
            float expansion = (i + 1) * 12f;
            float alpha = (0.4f - i * 0.08f) * (0.8f + 0.2f * MathUtils.sin(particleTime * 2f));

            shapeRenderer.setColor(COLOR_GLOW_DORADO.r, COLOR_GLOW_DORADO.g,
                    COLOR_GLOW_DORADO.b, alpha);
            shapeRenderer.rect(dialogX - expansion, dialogY - expansion,
                    DIALOG_WIDTH + 2 * expansion, DIALOG_HEIGHT + 2 * expansion);
        }

        shapeRenderer.end();
    }

    private void crearDialogoResultado() {
        resultDialog = new Dialog(game.bundle.get("juego.nivelcompletado"), skin) {
            @Override
            protected void result(Object object) {
                mostrandoVictoria = false;
            }
        };

        // --- CONFIGURACIÓN DEL DIÁLOGO PRINCIPAL ---
        resultDialog.getTitleLabel().setColor(COLOR_TEXTO_DESTACADO);
        resultDialog.getTitleLabel().setFontScale(0.5f);
        resultDialog.padTop(120); // <-- CAMBIO: Aumentado para bajar el título y que no se corte

        Table content = resultDialog.getContentTable();
        content.pad(10).padTop(25); // Un relleno general para el contenido

        // --- CÁLCULO DE DATOS ---
        int estrellasGanadas = calcularEstrellasPartida(movimientosFinal, tiempoFinal);
        String estrellasStr = generarStringEstrellas(estrellasGanadas);

        // --- ELEMENTOS DE LA UI ---
        // 1. Estrellas
        Label estrellasLabel = new Label(estrellasStr, skin, "lvltitle");
        estrellasLabel.setColor(COLOR_ESTRELLAS);
        estrellasLabel.setFontScale(2.3f);
        estrellasLabel.setAlignment(Align.center); // <-- CAMBIO: Aseguramos la alineación
        content.add(estrellasLabel).center().growX().padBottom(15);
        content.row();

        // 2. Primer Separador
        Label separador1 = new Label("-----------------", skin, "lvl");
        separador1.setColor(Color.BLACK);
        separador1.setAlignment(Align.center); // <-- CAMBIO: Forzamos el centrado del texto
        content.add(separador1).center().growX().pad(5, 20, 15, 20);
        content.row();

        // 3. Tabla de Estadísticas
        Table statsTable = new Table();

        // Fila de Movimientos
        Label movTitleLabel = new Label(game.bundle.get("juego.movimiento"), skin, "lvl");
        movTitleLabel.setColor(Color.BLACK);
        movTitleLabel.setFontScale(0.9f);
        statsTable.add(movTitleLabel).left(); // Alineado a la izquierda

        Label movLabel = new Label(movimientosFinal + " / " + movimientosPar, skin, "lvl");
        if (movimientosFinal <= movimientosPar) {
            movLabel.setColor(new Color(0.2f, 0.7f, 0.2f, 1f));
        } else {
            movLabel.setColor(new Color(0.8f, 0.5f, 0.1f, 1f));
        }
        movLabel.setFontScale(0.9f);
        statsTable.add(movLabel).right().expandX(); // Alineado a la derecha, expandiendo el espacio
        statsTable.row();

        // Fila de Tiempo
        Label tiempoTitleLabel = new Label(game.bundle.get("juego.tiempo"), skin, "lvl");
        tiempoTitleLabel.setColor(Color.BLACK);
        tiempoTitleLabel.setFontScale(0.9f);
        statsTable.add(tiempoTitleLabel).left().padTop(10); // Alineado a la izquierda

        Label tiempoResultLabel = new Label(formatearTiempo(tiempoFinal), skin, "lvl");
        tiempoResultLabel.setColor(Color.BLACK);
        tiempoResultLabel.setFontScale(0.9f);
        statsTable.add(tiempoResultLabel).right().expandX().padTop(10); // Alineado a la derecha

        // <-- CAMBIO CLAVE: Hacemos que la tabla de stats ocupe todo el ancho disponible -->
        content.add(statsTable).growX().padLeft(40).padRight(40).padBottom(15);
        content.row();

        // 4. Segundo Separador
        Label separador2 = new Label("-----------------", skin, "lvl");
        separador2.setColor(Color.BLACK);
        separador2.setAlignment(Align.center); // <-- CAMBIO: Forzamos el centrado del texto
        content.add(separador2).center().growX().pad(5, 20, 20, 20);
        content.row();

        // 5. Puntaje Final
        Label puntajeTitleLabel = new Label(game.bundle.get("juego.puntajefinal"), skin, "lvltitle");
        puntajeTitleLabel.setColor(Color.BLACK);
        puntajeTitleLabel.setFontScale(1.0f);
        content.add(puntajeTitleLabel).center().padBottom(5);
        content.row();

        Label puntajeLabel = new Label(String.valueOf(puntajeFinal), skin, "lvltitle");
        puntajeLabel.setColor(COLOR_ESTRELLAS);
        puntajeLabel.setFontScale(1.8f);
        content.add(puntajeLabel).center().padBottom(30); // Más espacio antes del botón
        content.row();

        // 6. Botón de Continuar
        TextButton continuarBtn = new TextButton(game.bundle.get("juego.continuar"), skin);
        continuarBtn.getLabel().setFontScale(0.8f);
        continuarBtn.getLabel().setColor(Color.BLACK);
        content.add(continuarBtn).center().width(200).height(50);

        continuarBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                mostrandoVictoria = false;
                resultDialog.hide();
            }
        });

        // --- CONFIGURACIÓN FINAL Y ANIMACIONES (Sin cambios aquí) ---
        resultDialog.setSize(DIALOG_WIDTH + 20, DIALOG_HEIGHT + 50);
        resultDialog.setPosition(
                (resultStage.getWidth() - resultDialog.getWidth()) / 2,
                (resultStage.getHeight() - resultDialog.getHeight()) / 2
        );

        resultDialog.setScale(0.5f);
        resultDialog.setColor(1, 1, 1, 0);
        resultDialog.addAction(Actions.sequence(
                Actions.delay(0.2f),
                Actions.parallel(
                        Actions.scaleTo(1.1f, 1.1f, 0.4f, Interpolation.elasticOut),
                        Actions.fadeIn(0.4f, Interpolation.pow2Out)
                ),
                Actions.scaleTo(1f, 1f, 0.2f, Interpolation.bounceOut)
        ));

        content.setColor(1, 1, 1, 0);
        content.addAction(Actions.delay(0.4f, Actions.fadeIn(0.5f, Interpolation.pow2Out)));

        resultStage.addActor(resultDialog);
    }

    private int calcularEstrellasPartida(int movimientos, long tiempo) {
        int estrellas = 1; // Mínimo por completar

        if (movimientos <= movimientosPar) {
            estrellas++;
        }
        if (tiempo <= ConfigNiveles.getTiempoObjetivo(nivelActual)) {
            estrellas++;
        }
        return estrellas;
    }

    private String generarStringEstrellas(int cantidad) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < cantidad) {
                sb.append("* ");
            } else {
                sb.append("- ");
            }
        }
        return sb.toString().trim();
    }

    private String formatearTiempo(long tiempoMs) {
        long segundos = tiempoMs / 1000;
        long minutos = segundos / 60;
        segundos = segundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    // Getters
    public int getMovimientosRealizados() {
        return movimientosRealizados;
    }

    public long getTiempoTranscurrido() {
        return System.currentTimeMillis() - tiempoInicio;
    }

    public long getTiempoTranscurridoReal(long tiempoPausado) {
        return System.currentTimeMillis() - tiempoInicio - tiempoPausado;
    }

    public int getScoreActual() {
        return 0; // Score se calcula al final
    }

    public void resize(int width, int height) {
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
        if (resultStage != null) {
            resultStage.getViewport().update(width, height, true);
        }
    }

    public void dispose() {
        if (uiStage != null) {
            uiStage.dispose();
        }
        if (resultStage != null) {
            resultStage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
