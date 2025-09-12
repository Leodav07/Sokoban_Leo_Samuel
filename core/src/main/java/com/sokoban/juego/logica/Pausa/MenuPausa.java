/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.Pausa;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
/**
 *
 * @author hnleo
 */
public class MenuPausa {
    private BitmapFont font;
    private BitmapFont titleFont;
    private ShapeRenderer shapeRenderer;
    private MenuPausaListener listener;
    
    private int opcionSeleccionada = 0;
    private boolean visible = false;
    
    private final Color COLOR_FONDO = new Color(0, 0, 0, 0.8f);
    private final Color COLOR_PANEL = new Color(0.2f, 0.2f, 0.2f, 0.95f);
    private final Color COLOR_TEXTO_NORMAL = Color.WHITE;
    private final Color COLOR_TEXTO_SELECCIONADO = Color.YELLOW;
    private final Color COLOR_TITULO = Color.CYAN;
    
    private final int PANEL_WIDTH = 400;
    private final int PANEL_HEIGHT = 300;
    private final int OPCION_HEIGHT = 40;
    
    public MenuPausa() {
        shapeRenderer = new ShapeRenderer();
        inicializarFuentes();
    }
    
    private void inicializarFuentes() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/arial.ttf")
            );
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = 
                new FreeTypeFontGenerator.FreeTypeFontParameter();
            
            parameter.size = 24;
            parameter.color = Color.WHITE;
            font = generator.generateFont(parameter);
            
            parameter.size = 32;
            parameter.color = COLOR_TITULO;
            titleFont = generator.generateFont(parameter);
            
            generator.dispose();
        } catch (Exception e) {
            font = new BitmapFont();
            titleFont = new BitmapFont();
            font.getData().setScale(1.5f);
            titleFont.getData().setScale(2.0f);
        }
    }
    
    public void setListener(MenuPausaListener listener) {
        this.listener = listener;
    }
    
    public void mostrar() {
        visible = true;
        opcionSeleccionada = 0;
    }
    
    public void ocultar() {
        visible = false;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void manejarInput() {
        if (!visible) return;
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || 
            Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            opcionSeleccionada--;
            if (opcionSeleccionada < 0) {
                opcionSeleccionada = OpcionPausa.getTotalOpciones() - 1;
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || 
            Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            opcionSeleccionada++;
            if (opcionSeleccionada >= OpcionPausa.getTotalOpciones()) {
                opcionSeleccionada = 0;
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || 
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            ejecutarOpcionSeleccionada();
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (listener != null) {
                listener.onContinuar();
            }
        }
    }
    
    private void ejecutarOpcionSeleccionada() {
        if (listener == null) return;
        
        OpcionPausa opcion = OpcionPausa.getOpcionPorIndice(opcionSeleccionada);
        
        switch (opcion) {
            case CONTINUAR:
                listener.onContinuar();
                break;
            case REINICIAR:
                listener.onReiniciarNivel();
                break;
            case MENU_PRINCIPAL:
                listener.onVolverMenuPrincipal();
                break;
            case SALIR_JUEGO:
                listener.onSalirJuego();
                break;
        }
    }
    
    public void dibujar(SpriteBatch batch) {
        if (!visible) return;
        
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        
        batch.end();
        
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(COLOR_FONDO);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        
        int panelX = (screenWidth - PANEL_WIDTH) / 2;
        int panelY = (screenHeight - PANEL_HEIGHT) / 2;
        
        shapeRenderer.setColor(COLOR_PANEL);
        shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COLOR_TITULO);
        shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        shapeRenderer.end();
        
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        
        batch.begin();
        
        dibujarTexto(batch, panelX, panelY);
    }
    
    private void dibujarTexto(SpriteBatch batch, int panelX, int panelY) {
        titleFont.setColor(COLOR_TITULO);
        titleFont.draw(batch, "PAUSA", 
                      panelX + (PANEL_WIDTH - 120) / 2, 
                      panelY + PANEL_HEIGHT - 50);
        
        int inicioY = panelY + PANEL_HEIGHT - 120;
        
        for (OpcionPausa opcion : OpcionPausa.values()) {
            Color color = (opcion.getIndice() == opcionSeleccionada) ? 
                         COLOR_TEXTO_SELECCIONADO : COLOR_TEXTO_NORMAL;
            
            font.setColor(color);
            
            String texto = opcion.getTexto();
            if (opcion.getIndice() == opcionSeleccionada) {
                texto = "> " + texto + " <";
            }
            
            font.draw(batch, texto, 
                     panelX + 50, 
                     inicioY - (opcion.getIndice() * OPCION_HEIGHT));
        }
        
        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.8f);
        font.draw(batch, " Arriba-Abajo -> Navegar | ENTER Seleccionar | ESC Continuar", 
                 panelX + 20, panelY + 30);
        font.getData().setScale(1.0f);
    }
    
    public void dispose() {
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
