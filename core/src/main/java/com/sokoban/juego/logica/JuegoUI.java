package com.sokoban.juego.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sokoban.juego.niveles.ConfigNiveles;

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

    private final Color COLOR_FONDO_UI = new Color(0.1f, 0.1f, 0.1f, 0.9f);
    private final int PANEL_HEIGHT = 120;

    public JuegoUI() {
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
        // Panel principal de UI
        uiPanel = new Table();
        uiPanel.setFillParent(false);
        uiPanel.setSize(800, PANEL_HEIGHT);
        uiPanel.setPosition(0, 0);
        uiStage.addActor(uiPanel);
        
        // Labels principales
        nivelLabel = new Label("", skin, "lvltitle");
        movimientosLabel = new Label("", skin, "lvl");
        tiempoLabel = new Label("", skin, "lvl");
        instruccionesLabel = new Label("", skin, "lvltitle");
        
        // Configurar escalas para mejor legibilidad
        nivelLabel.setFontScale(0.8f);
        movimientosLabel.setFontScale(0.6f);
        tiempoLabel.setFontScale(0.6f);
        instruccionesLabel.setFontScale(0.5f);
        
        // Layout del panel
        Table topRow = new Table();
        topRow.add(nivelLabel).expandX().left().pad(10);
        topRow.add(movimientosLabel).pad(10);
        topRow.add(tiempoLabel).pad(10);
        
        uiPanel.add(topRow).expandX().fillX().height(50);
        uiPanel.row();
        uiPanel.add(instruccionesLabel).expandX().left().pad(5, 10, 10, 10);
        
        // Animación inicial sutil
        uiPanel.setColor(1, 1, 1, 0);
        uiPanel.addAction(Actions.fadeIn(0.5f, Interpolation.pow2Out));
    }

    public void iniciarNivel(int nivel) {
        this.nivelActual = nivel;
        this.movimientosRealizados = 0;
        this.movimientosPar = ConfigNiveles.getMovimientosObjetivo(nivel);
        this.tiempoInicio = System.currentTimeMillis();
        
        // Actualizar labels
        nivelLabel.setText("Nivel " + nivelActual + ": " + ConfigNiveles.getNombreNivel(nivelActual));
        instruccionesLabel.setText("ESC/P: Pausa | R: Reiniciar | BACKSPACE: Deshacer");
        
        actualizarInformacion(0);
        
        // Animación de entrada para el nuevo nivel
        uiPanel.addAction(Actions.sequence(
            Actions.scaleTo(0.9f, 0.9f, 0.1f),
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.bounceOut)
        ));
    }

    public void incrementarMovimientos() {
        movimientosRealizados++;
        actualizarInformacion(0);
        
        // Animación sutil en el contador de movimientos
        movimientosLabel.addAction(Actions.sequence(
            Actions.scaleTo(1.1f, 1.1f, 0.1f),
            Actions.scaleTo(1f, 1f, 0.1f)
        ));
    }
    
    public void decrementarMovimientos() {
        if (movimientosRealizados > 0) {
            movimientosRealizados--;
            actualizarInformacion(0);
            
            // Animación para undo
            movimientosLabel.addAction(Actions.sequence(
                Actions.color(Color.YELLOW, 0.2f),
                Actions.color(Color.WHITE, 0.3f)
            ));
        }
    }

    private void actualizarInformacion(long tiempoPausado) {
        movimientosLabel.setText("Movimientos: " + movimientosRealizados + " (Par: " + movimientosPar + ")");
        
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio - tiempoPausado;
        tiempoLabel.setText("Tiempo: " + formatearTiempo(tiempoTranscurrido));
    }

    public void setResultadoFinal(int puntaje, int movimientos, long tiempo) {
        this.puntajeFinal = puntaje;
        this.movimientosFinal = movimientos;
        this.tiempoFinal = tiempo;
    }

    public void dibujar(SpriteBatch batch, int gameWorldWidth, int gameWorldHeight, long tiempoPausado) {
        // Dibujar fondo del panel
        batch.end();
        
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_FONDO_UI);
        shapeRenderer.rect(0, 0, gameWorldWidth, PANEL_HEIGHT);
        shapeRenderer.end();
        
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        
        batch.begin();
        
        // Actualizar información en tiempo real
        actualizarInformacion(tiempoPausado);
        
        // Dibujar UI
        uiStage.getViewport().apply();
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }

    public void mostrarResultadoNivel(SpriteBatch batch) {
        if (resultDialog == null) {
            crearDialogoResultado();
        }
        
        // Dibujar el diálogo de resultado
        resultStage.getViewport().apply();
        resultStage.act(Gdx.graphics.getDeltaTime());
        resultStage.draw();
    }

    private void crearDialogoResultado() {
        resultDialog = new Dialog("¡NIVEL COMPLETADO!", skin) {
            @Override
            protected void result(Object object) {
                // El resultado se maneja en MapaBase
            }
        };
        
        // Calcular estrellas
        int estrellasGanadas = calcularEstrellasPartida(movimientosFinal, tiempoFinal);
        String estrellasStr = generarStringEstrellas(estrellasGanadas);
        
        // Contenido del diálogo
        Table content = new Table();
        
        // Estrellas
        Label estrellasLabel = new Label(estrellasStr, skin, "lvl");
        estrellasLabel.setColor(Color.YELLOW);
        estrellasLabel.setFontScale(1.0f);
        content.add(estrellasLabel).colspan(2).padBottom(15);
        content.row();
        
        // Primera fila de información
        content.add(new Label("Movimientos:", skin, "lvl")).left().padRight(15);
        content.add(new Label(movimientosFinal + " (Par: " + movimientosPar + ")", skin, "default")).left();
        content.row();
        
        content.add(new Label("Tiempo:", skin, "lvl")).left().padRight(15).padTop(8);
        content.add(new Label(formatearTiempo(tiempoFinal), skin, "default")).left().padTop(8);
        content.row();
        
        // Segunda fila - Puntaje centrado
        content.add(new Label("Puntaje Final:", skin, "lvl")).colspan(2).center().padTop(15);
        content.row();
        Label puntajeLabel = new Label(String.valueOf(puntajeFinal), skin, "lvl");
        puntajeLabel.setColor(Color.YELLOW);
        puntajeLabel.setFontScale(1.2f);
        content.add(puntajeLabel).colspan(2).center().padBottom(15);
        content.row();
        
        resultDialog.getContentTable().add(content).pad(20);
        resultDialog.button("CONTINUAR", true);
        
        // Configurar tamaño y posición - hacer el diálogo un poco más alto
        resultDialog.setSize(400, 380);
        resultDialog.setPosition(200, 50); // Centrado en pantalla 800x480
        
        // Animación de entrada
        resultDialog.setScale(0.8f);
        resultDialog.setColor(1, 1, 1, 0);
        resultDialog.addAction(Actions.parallel(
            Actions.scaleTo(1f, 1f, 0.3f, Interpolation.bounceOut),
            Actions.fadeIn(0.3f)
        ));
        
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
                sb.append("★ ");
            } else {
                sb.append("☆ ");
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
        if (uiStage != null) uiStage.dispose();
        if (resultStage != null) resultStage.dispose();
        if (skin != null) skin.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
