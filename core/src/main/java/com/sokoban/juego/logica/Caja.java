package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * @author hnleo
 */
public class Caja extends Elemento {

    private int tileSize;
    private Texture texturaNormal;     
    private Texture texturaEnObjetivo;  
    private boolean estaEnObjetivo;    

    public Caja(int x, int y, Texture textura, int tileSize) {
        super(x, y, textura);
        this.tileSize = tileSize;
        this.texturaNormal = textura;
        this.texturaEnObjetivo = textura; 
        this.estaEnObjetivo = false;
    }

    public Caja(int x, int y, Texture texturaNormal, Texture texturaEnObjetivo, int tileSize) {
        super(x, y, texturaNormal);
        this.tileSize = tileSize;
        this.texturaNormal = texturaNormal;
        this.texturaEnObjetivo = texturaEnObjetivo;
        this.estaEnObjetivo = false;
    }

    
    public void setEstaEnObjetivo(boolean enObjetivo) {
        this.estaEnObjetivo = enObjetivo;
        if (enObjetivo) {
            this.textura = texturaEnObjetivo;
        } else {
            this.textura = texturaNormal;
        }
    }

   
    public boolean estaEnObjetivo() {
        return estaEnObjetivo;
    }

   
    public void setTexturaEnObjetivo(Texture texturaEnObjetivo) {
        this.texturaEnObjetivo = texturaEnObjetivo;
        // Si actualmente está en objetivo, actualizar la textura
        if (estaEnObjetivo) {
            this.textura = texturaEnObjetivo;
        }
    }

    public void setTexturaNormal(Texture texturaNormal) {
        this.texturaNormal = texturaNormal;
        if (!estaEnObjetivo) {
            this.textura = texturaNormal;
        }
    }

    @Override
    public void dibujar(SpriteBatch batch, int tileSize, int offsetX, int offsetY, int filas) {
        batch.draw(textura, offsetX + x * tileSize, offsetY + y * tileSize, tileSize, tileSize);
    }
}