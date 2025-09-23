/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.Mapas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sokoban.juego.Main;
import com.sokoban.juego.logica.GestorMapeo;
import com.sokoban.juego.logica.GestorDatosPerfil;
import com.sokoban.juego.logica.Jugador;
/**
 *
 * @author hnleo
 */
public class MapaTutorial extends MapaBase{
     private enum EstadoTutorial {
        INICIO,
        ESPERANDO_ARRIBA,
        ESPERANDO_ABAJO,
        ESPERANDO_IZQUIERDA,
        ESPERANDO_DERECHA,
        MOSTRAR_EMPUJAR,
        ESPERANDO_EMPUJE,
        MOSTRAR_OBJETIVO,
        ESPERANDO_CAJA_EN_OBJETIVO,
        MOSTRAR_FINAL,
        FINALIZADO
    }

    private EstadoTutorial estadoActual;
    private String mensajeActual = "";
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout layout = new GlyphLayout();
    private Main game;
    private final int[][] tutorialLayout = {
        {5,5,5,5,5,5,5,5,5,5},
        {5,5,5,5,5,5,5,5,5,5},
        {1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,4,0,0,2,0,0,3,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1},
        {5,5,5,5,5,5,5,5,5,5},
        {5,5,5,5,5,5,5,5,5,5},
    };

    public MapaTutorial(Texture muro, Texture caja, Texture meta, Texture suelo, Texture jugador, Texture cajaObj, Texture fondo, Main game) {
        super(9, 10, muro, caja, meta, suelo, jugador, cajaObj, fondo, 0, game);
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.5f);
        this.estadoActual = EstadoTutorial.INICIO;
        this.game = game;
        cambiarEstado(EstadoTutorial.INICIO);
    }
    
    @Override
    protected int[][] getLayout() { return tutorialLayout; }
    @Override
    protected int getMovimientosObjetivo() { return 0; }
    @Override
    protected long getTiempoObjetivo() { return 0; }

    private void cambiarEstado(EstadoTutorial nuevoEstado) {
        this.estadoActual = nuevoEstado;
        switch (nuevoEstado) {
            case INICIO:
                mensajeActual = game.bundle.get("tuto.bienvenido1") + Input.Keys.toString(GestorMapeo.ARRIBA) + game.bundle.get("tuto.bienvenido2");
                break;
            case ESPERANDO_ARRIBA:
                break;
            case ESPERANDO_ABAJO:
                mensajeActual = game.bundle.get("tuto.esperandoabajo") + Input.Keys.toString(GestorMapeo.ABAJO) + game.bundle.get("tuto.esperandoabajo2");
                break;
            case ESPERANDO_IZQUIERDA:
                mensajeActual = game.bundle.get("tuto.esperandoizquierda") + Input.Keys.toString(GestorMapeo.IZQUIERDA) + game.bundle.get("tuto.esperandoizquierda2");
                break;
            case ESPERANDO_DERECHA:
                mensajeActual = game.bundle.get("tuto.casilotienes") + Input.Keys.toString(GestorMapeo.DERECHA) + game.bundle.get("tuto.casilotienes2");
                break;
            case MOSTRAR_EMPUJAR:
                mensajeActual =game.bundle.get("tuto.objetivo");
                break;
            case MOSTRAR_OBJETIVO:
                mensajeActual = game.bundle.get("tuto.llevar");
                break;
            case MOSTRAR_FINAL:
                mensajeActual = game.bundle.get("tuto.completado");
                break;
        }
    }

    @Override
    public void update(float delta) {
        verificarTeclasTutorial();
        jugador.update(delta);
    }
    
    public void verificarTeclasTutorial() {
        if (jugador.estaMoviendose()) return;

        switch (estadoActual) {
            case INICIO:
            case ESPERANDO_ARRIBA:
                if (Gdx.input.isKeyJustPressed(GestorMapeo.ARRIBA)) {
                    jugador.cambiarDireccion(Jugador.DireccionMovimiento.ABAJO);
                    motorMovimiento.moverJugador(0, -1);
                    cambiarEstado(EstadoTutorial.ESPERANDO_ABAJO);
                }
                break;
            case ESPERANDO_ABAJO:
                if (Gdx.input.isKeyJustPressed(GestorMapeo.ABAJO)) {
                   jugador.cambiarDireccion(Jugador.DireccionMovimiento.ARRIBA);
                    motorMovimiento.moverJugador(0, 1);
                    cambiarEstado(EstadoTutorial.ESPERANDO_IZQUIERDA);
                }
                break;
            case ESPERANDO_IZQUIERDA:
                 if (Gdx.input.isKeyJustPressed(GestorMapeo.IZQUIERDA)) {
                     jugador.cambiarDireccion(Jugador.DireccionMovimiento.IZQUIERDA);
                    motorMovimiento.moverJugador(-1, 0);
                    cambiarEstado(EstadoTutorial.ESPERANDO_DERECHA);
                }
                break;
            case ESPERANDO_DERECHA:
                if (Gdx.input.isKeyJustPressed(GestorMapeo.DERECHA)) {
                    jugador.cambiarDireccion(Jugador.DireccionMovimiento.DERECHA);
                    motorMovimiento.moverJugador(1, 0);
                    cambiarEstado(EstadoTutorial.MOSTRAR_EMPUJAR);
                }
                break;
            case MOSTRAR_EMPUJAR:
            case ESPERANDO_EMPUJE:
            case MOSTRAR_OBJETIVO:
            case ESPERANDO_CAJA_EN_OBJETIVO:
                procesarMovimientoLibre();
                break;
            case MOSTRAR_FINAL:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    finalizarTutorial();
                }
                break;
        }
    }

    private void procesarMovimientoLibre() {
        int dx = 0, dy = 0;
        if (Gdx.input.isKeyJustPressed(GestorMapeo.ARRIBA)) dy = -1;
        if (Gdx.input.isKeyJustPressed(GestorMapeo.ABAJO)) dy = 1;
        if (Gdx.input.isKeyJustPressed(GestorMapeo.IZQUIERDA)) dx = -1;
        if (Gdx.input.isKeyJustPressed(GestorMapeo.DERECHA)) dx = 1;

        if (dx != 0 || dy != 0) {
            boolean seMovio = motorMovimiento.moverJugador(dx, dy);
            if (seMovio && motorMovimiento.nivelCompletado()) {
                 cambiarEstado(EstadoTutorial.MOSTRAR_FINAL);
            } else if (seMovio && estadoActual == EstadoTutorial.MOSTRAR_EMPUJAR){
                cambiarEstado(EstadoTutorial.MOSTRAR_OBJETIVO);
            }
        }
    }

    private void finalizarTutorial() {
        estadoActual = EstadoTutorial.FINALIZADO;
        mensajeActual = "";
        GestorDatosPerfil.getInstancia().marcarTutorialCompletado();
        if(mapaListener != null) {
            mapaListener.onNivelFinalizado();
        }
    }

    @Override
    public void dibujar(SpriteBatch batch) {
        super.dibujar(batch); 

    
        if (!mensajeActual.isEmpty()) {
            batch.end(); 
            
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
            
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            layout.setText(font, mensajeActual);
            float textWidth = layout.width;
            float textHeight = layout.height;
            float padding = 20f;
            
            float rectWidth = textWidth + padding * 2;
            float rectHeight = textHeight + padding * 2;
            float rectX = (800 - rectWidth) / 2; 
            float rectY = 20; 
            
            shapeRenderer.setColor(0, 0, 0, 0.7f);
            shapeRenderer.rect(rectX, rectY, rectWidth, rectHeight);
            
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
            
            batch.begin(); 
            font.draw(batch, mensajeActual, rectX + padding, rectY + rectHeight - padding);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
