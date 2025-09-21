package com.sokoban.juego.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sokoban.juego.niveles.ConfigNiveles;

public class JuegoUI {

    private BitmapFont font;
    private BitmapFont titleFont;
    private ShapeRenderer shapeRenderer;
    private int nivelActual;
    private int movimientosRealizados;
    private int movimientosPar;
    private long tiempoInicio;
    private int puntajeFinal, movimientosFinal;
    private long tiempoFinal;
    private final Color COLOR_TITULO = new Color(1f, 0.84f, 0f, 1f);
    private final Color COLOR_FONDO_UI = new Color(0.1f, 0.1f, 0.1f, 0.8f);
    private final Color COLOR_TEXTO_NORMAL = Color.WHITE;

    private final int PANEL_HEIGHT = 120;
    private final int MARGIN = 20;

    public JuegoUI() {
        shapeRenderer = new ShapeRenderer();
        inicializarFuentes();
    }

    private void inicializarFuentes() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 24;
            parameter.color = Color.WHITE;
            font = generator.generateFont(parameter);
            parameter.size = 36;
            parameter.color = COLOR_TITULO;
            titleFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            font = new BitmapFont();
            titleFont = new BitmapFont();
        }
    }

    public void iniciarNivel(int nivel) {
        this.nivelActual = nivel;
        this.movimientosRealizados = 0;
        this.movimientosPar = ConfigNiveles.getMovimientosObjetivo(nivel);
        this.tiempoInicio = System.currentTimeMillis();
    }

    public void incrementarMovimientos() {
        movimientosRealizados++;
    }

    public void setResultadoFinal(int puntaje, int movimientos, long tiempo) {
        this.puntajeFinal = puntaje;
        this.movimientosFinal = movimientos;
        this.tiempoFinal = tiempo;
    }

    public void dibujar(SpriteBatch batch, int gameWorldWidth, int gameWorldHeight, long tiempoPausado) {
        batch.end();

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_FONDO_UI);
        shapeRenderer.rect(0, 0, gameWorldWidth, PANEL_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        batch.begin();
        dibujarInformacionJuego(batch, tiempoPausado);
    }

    private void dibujarInformacionJuego(SpriteBatch batch, long tiempoPausado) {
        int x = MARGIN;
        int y = PANEL_HEIGHT - 30;

        titleFont.setColor(COLOR_TITULO);
        titleFont.draw(batch, "Nivel " + nivelActual + ": " + ConfigNiveles.getNombreNivel(nivelActual), x, y);

        x += 350;
        font.setColor(COLOR_TEXTO_NORMAL);
        font.draw(batch, "Movimientos: " + movimientosRealizados + " (Par: " + movimientosPar + ")", x, y);

        x += 300;
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio - tiempoPausado;
        String tiempoStr = formatearTiempo(tiempoTranscurrido);
        font.draw(batch, "Tiempo: " + tiempoStr, x, y);

        y -= 40;
        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.8f);
        font.draw(batch, "ESC/P: Pausa | R: Reiniciar | BACKSPACE: Deshacer", MARGIN, y);
        font.getData().setScale(1.0f);
    }

    public void mostrarResultadoNivel(SpriteBatch batch) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        batch.end();
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);

        int panelWidth = 400;
        int panelHeight = 300; 
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = (screenHeight - panelHeight) / 2;

        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.9f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        batch.begin();

        int textY = panelY + panelHeight - 40;

        titleFont.setColor(COLOR_TITULO);
        titleFont.draw(batch, "¡NIVEL COMPLETADO!", panelX + 50, textY);
        textY -= 50;

        int estrellasGanadas = calcularEstrellasPartida(movimientosFinal, tiempoFinal);
        String estrellasStr = generarStringEstrellas(estrellasGanadas);

        titleFont.setColor(Color.YELLOW); // Usamos la fuente grande para las estrellas
        titleFont.draw(batch, estrellasStr, panelX + 140, textY);
        textY -= 40;

        font.setColor(COLOR_TEXTO_NORMAL);
        font.draw(batch, "Movimientos: " + movimientosFinal + " (Par: " + movimientosPar + ")", panelX + 50, textY);
        textY -= 30;

        font.draw(batch, "Tiempo: " + formatearTiempo(tiempoFinal), panelX + 50, textY);
        textY -= 30;

        font.setColor(Color.YELLOW);
        font.draw(batch, "Puntaje Final: " + puntajeFinal, panelX + 50, textY);
        textY -= 50;

        font.setColor(Color.WHITE);
        font.draw(batch, "Presiona ENTER para continuar", panelX + 50, textY);
    }

    private int calcularEstrellasPartida(int movimientos, long tiempo) {
        int estrellas = 1;

        int movimientosPar = ConfigNiveles.getMovimientosObjetivo(nivelActual);
        long tiempoObjetivo = ConfigNiveles.getTiempoObjetivo(nivelActual);

        if (movimientos <= movimientosPar) {
            estrellas++;
        }
        if (tiempo <= tiempoObjetivo) {
            estrellas++;
        }
        return estrellas;
    }

    private String generarStringEstrellas(int cantidad) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < cantidad) {
                sb.append("*");
            } else {
                sb.append("O");
            }
        }
        return sb.toString();
    }

    private String formatearTiempo(long tiempoMs) {
        long segundos = tiempoMs / 1000;
        long minutos = segundos / 60;
        segundos = segundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

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
        return 0; // El score ya no se calcula en tiempo real
    }

    public void decrementarMovimientos() {
        if (movimientosRealizados > 0) {
            movimientosRealizados--;
        }
    }

    public void dispose() {
        if (font != null) {
            font.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
