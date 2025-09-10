/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 *
 * @author hnleo
 */
public class Caja {
    private float x, y;
    private Rectangle bounds;
    private Texture textura;
    
    public Caja(float x, float y){
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x, y, 64, 64);
        this.textura = new Texture("caja.PNG");
    }
    
    public void mover(float dx, float dy){
        x+= dx;
        y += dy;
        bounds.setPosition(x, y);
    }
    
    public Rectangle getBounds(){
        return bounds;
    }
    
    public void render(SpriteBatch batch){
        batch.draw(textura, x, y);
    }
    public void dispose(){
        textura.dispose();
    }
            
    
}
