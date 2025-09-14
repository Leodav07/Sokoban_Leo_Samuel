package com.sokoban.juego.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sokoban.juego.logica.accounts.GestorProgreso;
import com.sokoban.juego.logica.accounts.ProgresoPorNivel;

public class JuegoUI {

    private BitmapFont font;
    private BitmapFont titleFont;
    private ShapeRenderer shapeRenderer;
    private int nivelActual;
    private int movimientosRealizados;
    private int movimientosObjetivo;
    private long tiempoInicio;
    private long tiempoObjetivo;
    private int scoreActual;
    private int scoreMaximo;
    private final Color COLOR_TITULO = new Color(1f, 0.84f, 0f, 1f);

    private final Color COLOR_FONDO_UI = new Color(0.1f, 0.1f, 0.1f, 0.8f);
    private final Color COLOR_TEXTO_NORMAL = Color.WHITE;
    private final Color COLOR_TEXTO_BUENO = Color.GREEN;
    private final Color COLOR_TEXTO_REGULAR = Color.YELLOW;
    private final Color COLOR_TEXTO_MALO = Color.RED;

    private final int PANEL_HEIGHT = 120;
    private final int MARGIN = 20;

    public JuegoUI() {
        shapeRenderer = new ShapeRenderer();
        inicializarFuentes();
    }

    private void inicializarFuentes() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                    Gdx.files.internal("fonts/arial.ttf")
            );
            FreeTypeFontGenerator.FreeTypeFontParameter parameter
                    = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 24;
            parameter.color = Color.WHITE;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            parameter.gamma = 1.5f;
            parameter.borderWidth = 0.5f;
            parameter.borderColor = Color.BLACK;
            parameter.borderGamma = 1.5f;
            font = generator.generateFont(parameter);
            parameter.size = 36;
            parameter.color = COLOR_TITULO;
            titleFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            font = new BitmapFont();
            titleFont = new BitmapFont();
            font.getData().setScale(1.2f);
            titleFont.getData().setScale(1.5f);
        }
    }

    public void iniciarNivel(int nivel, int movimientosParaTresEstrellas, long tiempoParaTresEstrellas) {
        this.nivelActual = nivel;
        this.movimientosRealizados = 0;
        this.movimientosObjetivo = movimientosParaTresEstrellas;
        this.tiempoInicio = System.currentTimeMillis();
        this.tiempoObjetivo = tiempoParaTresEstrellas;
        this.scoreMaximo = 2000;
        actualizarScore(0);
    }

    public void incrementarMovimientos() {
        movimientosRealizados++;
        actualizarScore(0);
    }

    private void actualizarScore(long tiempoPausado) {
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio - tiempoPausado;

        int scoreMovimientos = Math.max(0, 1000 - ((movimientosRealizados - movimientosObjetivo) * 20));
        int scoreTiempo = Math.max(0, (int) (1000 - ((tiempoTranscurrido - tiempoObjetivo) / 1000) * 10));

        scoreMovimientos = Math.max(scoreMovimientos, 0);
        scoreTiempo = Math.max(scoreTiempo, 0);

        scoreActual = scoreMovimientos + scoreTiempo;
    }

    public void dibujar(SpriteBatch batch, int gameWorldWidth, int gameWorldHeight, long tiempoPausado) {
        int panelY = 0;

        batch.end();

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_FONDO_UI);
        shapeRenderer.rect(0, panelY, gameWorldWidth, PANEL_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        batch.begin();

        actualizarScore(tiempoPausado);
        dibujarInformacionJuego(batch, panelY, gameWorldWidth, tiempoPausado);
    }

   private void dibujarInformacionJuego(SpriteBatch batch, int panelY, int gameWorldWidth, long tiempoPausado) {
        int x = MARGIN;
        int y = panelY + PANEL_HEIGHT - 30;

        titleFont.setColor(COLOR_TITULO);
        titleFont.draw(batch, "Nivel " + nivelActual, x, y);

        x += 150;
        Color colorMovimientos = getColorMovimientos();
        font.setColor(colorMovimientos);
        font.draw(batch, "Movimientos: " + movimientosRealizados + "/" + movimientosObjetivo, x, y);

        x += 220;
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio - tiempoPausado;
        String tiempoStr = formatearTiempo(tiempoTranscurrido);
        String tiempoObjetivoStr = formatearTiempo(tiempoObjetivo);

        Color colorTiempo = getColorTiempo(tiempoTranscurrido);
        font.setColor(colorTiempo);
        font.draw(batch, "Tiempo: " + tiempoStr + "/" + tiempoObjetivoStr, x, y);

        x += 200;
        if (x + 150 < gameWorldWidth - MARGIN) {
            Color colorScore = getColorScore();
            font.setColor(colorScore);
            font.draw(batch, "Score: " + scoreActual + "/" + scoreMaximo, x, y);
        }

        x = MARGIN;
        y -= 40;

        int estrellasPredichas = calcularEstrellas(tiempoPausado);
        font.setColor(COLOR_TEXTO_NORMAL);
        font.draw(batch, "Estrellas: " + generarStringEstrellas(estrellasPredichas), x, y);

        y -= 25;
        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.7f);
        font.draw(batch, "ESC/P: Pausa | R: Reiniciar | BACKSPACE: Deshacer | TAB: Estadísticas", x, y);
        font.getData().setScale(1.0f);

        font.setColor(COLOR_TEXTO_NORMAL);
    }

    private Color getColorMovimientos() {
        if (movimientosRealizados <= movimientosObjetivo) {
            return COLOR_TEXTO_BUENO;
        } else if (movimientosRealizados <= movimientosObjetivo * 1.5f) {
            return COLOR_TEXTO_REGULAR;
        } else {
            return COLOR_TEXTO_MALO;
        }
    }

    private Color getColorTiempo(long tiempoTranscurrido) {
        if (tiempoTranscurrido <= tiempoObjetivo) {
            return COLOR_TEXTO_BUENO;
        } else if (tiempoTranscurrido <= tiempoObjetivo * 1.5f) {
            return COLOR_TEXTO_REGULAR;
        } else {
            return COLOR_TEXTO_MALO;
        }
    }

    private Color getColorScore() {
        float porcentajeScore = (float) scoreActual / scoreMaximo;
        if (porcentajeScore >= 0.8f) {
            return COLOR_TEXTO_BUENO;
        } else if (porcentajeScore >= 0.5f) {
            return COLOR_TEXTO_REGULAR;
        } else {
            return COLOR_TEXTO_MALO;
        }
    }

    public int calcularEstrellas(long tiempoPausado) {
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio - tiempoPausado;

        int estrellasMovimientos = 0;
        int estrellasTiempo = 0;

        if (movimientosRealizados <= movimientosObjetivo) {
            estrellasMovimientos = 3;
        } else if (movimientosRealizados <= movimientosObjetivo * 1.3f) {
            estrellasMovimientos = 2;
        } else if (movimientosRealizados <= movimientosObjetivo * 1.6f) {
            estrellasMovimientos = 1;
        }

        if (tiempoTranscurrido <= tiempoObjetivo) {
            estrellasTiempo = 3;
        } else if (tiempoTranscurrido <= tiempoObjetivo * 1.3f) {
            estrellasTiempo = 2;
        } else if (tiempoTranscurrido <= tiempoObjetivo * 1.6f) {
            estrellasTiempo = 1;
        }

        return Math.max(1, (estrellasMovimientos + estrellasTiempo) / 2);
    }

    public int calcularEstrellas() {
        return calcularEstrellas(0);
    }

    private String generarStringEstrellas(int cantidad) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < cantidad) {
                sb.append("*"); // Estrella llena
            } else {
                sb.append("°"); // Estrella vacía
            }
        }
        return sb.toString();
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

        int textY = panelY + panelHeight - 50;
        int estrellas = calcularEstrellas(0);

        titleFont.setColor(COLOR_TITULO);
        titleFont.draw(batch, "¡NIVEL COMPLETADO!", panelX + 50, textY);
        textY -= 50;

        font.setColor(COLOR_TEXTO_NORMAL);
        font.draw(batch, "Estrellas: " + generarStringEstrellas(estrellas), panelX + 50, textY);
        textY -= 30;

        font.draw(batch, "Score: " + scoreActual, panelX + 50, textY);
        textY -= 30;

        font.draw(batch, "Movimientos: " + movimientosRealizados, panelX + 50, textY);
        textY -= 30;

        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
        font.draw(batch, "Tiempo: " + formatearTiempo(tiempoTranscurrido), panelX + 50, textY);
        textY -= 50;

        font.draw(batch, "Presiona ENTER para continuar", panelX + 50, textY);
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
        return scoreActual;
    }

    public void decrementarMovimientos() {
        if (movimientosRealizados > 0) {
            movimientosRealizados--;
        }
    }

    
    public void actualizarScoreSinTiempo() {
        int scoreMovimientos = Math.max(0, 1000 - ((movimientosRealizados - movimientosObjetivo) * 20));

        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
        int scoreTiempo = Math.max(0, (int) (1000 - ((tiempoTranscurrido - tiempoObjetivo) / 1000) * 10));

        scoreActual = scoreMovimientos + scoreTiempo;

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
