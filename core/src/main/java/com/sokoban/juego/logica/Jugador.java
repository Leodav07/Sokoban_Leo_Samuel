/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 *
 * @author hnleo
 */
public class Jugador extends Elemento{
    float posX, posY;
    float velocidad = 384f;
    float targetX, targetY;
    boolean moviendose = false;
    
    public Jugador(int x, int y, Texture textura) {
        super(x, y, textura);
        this.posX = x * 64f;
        this.posY = y * 64f;
        this.targetX = posX;
        this.targetY = posY;
    }
    
    public void moverCelda(int nuevoX, int nuevoY){
        x = nuevoX;
        y = nuevoY;
        targetX = x * 64f;
        targetY = y * 64f;
        moviendose = true;
    }
    
    public boolean estaMoviendose(){
        return moviendose;
    }
    
    public void update(float delta){
         if (!moviendose) return;
        
        float distanciaX = targetX - posX;
        float distanciaY = targetY - posY;
        
        if (Math.abs(distanciaX) < 2f && Math.abs(distanciaY) < 2f) {
            posX = targetX;
            posY = targetY;
            moviendose = false;
            return;
        }
        
        float factor = MathUtils.clamp(velocidad * delta / 64f, 0f, 1f);
        posX = MathUtils.lerp(posX, targetX, factor);
        posY = MathUtils.lerp(posY, targetY, factor);
    }
    
    public void dibujar(SpriteBatch batch, int tamCelda, int offsetX, int offsetY, int filas) {
        float drawX = Math.round(offsetX + posX);
        float drawY = Math.round(offsetY + (filas * tamCelda - tamCelda - posY));
        
        batch.draw(textura, drawX, drawY, tamCelda, tamCelda);
    }
}