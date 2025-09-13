/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.awt.Image;

/**
 *
 * @author hnleo
 */
public class Caja extends Elemento {

    private int tileSize;

    public Caja(int x, int y, Texture textura, int tileSize) {
        super(x, y, textura);
        this.tileSize = tileSize;
    }

    @Override
    public void dibujar(SpriteBatch batch, int tileSize, int offsetX, int offsetY, int filas) {
        batch.draw(textura, offsetX + x * tileSize, offsetY + y * tileSize, tileSize, tileSize);
    }

}
