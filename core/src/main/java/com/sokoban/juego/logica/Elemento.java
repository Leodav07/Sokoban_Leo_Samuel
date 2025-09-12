/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 *
 * @author hnleo
 */
public class Elemento {
    int x, y;
    Texture textura;
    
    public Elemento(int x, int y, Texture textura){
        this.x = x;
        this.y = y;
        this.textura = textura;
    }
    
   public void dibujar(SpriteBatch batch, int tamCelda, int offsetX, int offsetY, int filas) {
        batch.draw(textura, offsetX + x * tamCelda, offsetY + (filas - 1 - y) * tamCelda, tamCelda, tamCelda);
    }
}
