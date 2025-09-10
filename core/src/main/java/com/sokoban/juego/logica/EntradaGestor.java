/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
/**
 *
 * @author hnleo
 */
public class EntradaGestor {
     private Jugador jugador;
    private TileMapRun tileMap;
    private float tiempoUltimoMovimiento = 0f;
    private static final float DELAY = 0.08f; 
    
    public EntradaGestor(Jugador jugador, TileMapRun tileMap) {
        this.jugador = jugador;
        this.tileMap = tileMap;
    }
    
    public void update(float deltaTime) {
        tiempoUltimoMovimiento += deltaTime;
        
        if (tiempoUltimoMovimiento < DELAY) {
            return;
        }
        
        boolean seMovio = false;
        
        if ( Gdx.input.isKeyPressed(Input.Keys.W)) {
            if (jugador.mover(Direccion.UP, deltaTime, tileMap)) {
                seMovio = true;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            if (jugador.mover(Direccion.DOWN, deltaTime, tileMap)) {
                seMovio = true;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (jugador.mover(Direccion.LEFT, deltaTime, tileMap)) {
                seMovio = true;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (jugador.mover(Direccion.RIGHT, deltaTime, tileMap)) {
                seMovio = true;
            }
        }
        
        if (seMovio) {
            tiempoUltimoMovimiento = 0f;
            jugador.setEstaMoviendo(true);
        } else {
            jugador.setEstaMoviendo(false);
        }
    }
    
    public void setTileMap(TileMapRun tileMap) {
        this.tileMap = tileMap;
    }
}
