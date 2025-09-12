/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
//
//
//public class Jugador {
//
//    private float x, y;
//    private Rectangle bounds;
//    private float velocidad;
//    private boolean estaMoviendo = false;
//    private Direccion direccionActual;
//
//    private Animation<TextureRegion> animacionCaminarArriba;
//    private Animation<TextureRegion> animacionCaminarAbajo;
//    private Animation<TextureRegion> animacionCaminarIzquierda;
//    private Animation<TextureRegion> animacionCaminarDerecha;
//    private Animation<TextureRegion> animacionIdle;
//
//    private float tiempoAnimacion = 0f;
//    private Texture spriteSheet;
//
//    private static final int FRAME_WIDTH = 40;
//    private static final int FRAME_HEIGHT = 64;
//    private static final float FRAME_DURATION = 0.08f;
//
//    public Jugador(float x, float y) {
//        this.x = x;
//        this.y = y;
//        this.velocidad = 1050f;
//        this.bounds = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
//        this.direccionActual = Direccion.DOWN;
//
//        cargarAnimaciones();
//    }
//
//    private void cargarAnimaciones() {
//        spriteSheet = new Texture("jugador_spritesheet.png");
//
//        if (spriteSheet == null) {
//            System.out.println("NO ENCONTRE EL ARCHIVO");
//        } else {
//            System.out.println("si lo encontre");
//        }
//
//        TextureRegion[][] frames = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
//
//        TextureRegion[] framesCaminarAbajo = new TextureRegion[3];
//        for (int i = 0; i < 3; i++) {
//            framesCaminarAbajo[i] = frames[2][i];
//        }
//        animacionCaminarAbajo = new Animation<>(FRAME_DURATION, framesCaminarAbajo);
//
//        TextureRegion[] framesCaminarArriba = new TextureRegion[3];
//        for (int i = 0; i < 3; i++) {
//            framesCaminarArriba[i] = frames[0][i];
//        }
//        animacionCaminarArriba = new Animation<>(FRAME_DURATION, framesCaminarArriba);
//
//        TextureRegion[] framesCaminarIzquierda = new TextureRegion[3];
//        for (int i = 0; i < 3; i++) {
//            framesCaminarIzquierda[i] = frames[3][i];
//        }
//        animacionCaminarIzquierda = new Animation<>(FRAME_DURATION, framesCaminarIzquierda);
//
//        TextureRegion[] framesCaminarDerecha = new TextureRegion[3];
//        for (int i = 0; i < 3; i++) {
//            framesCaminarDerecha[i] = frames[1][i];
//        }
//        animacionCaminarDerecha = new Animation<>(FRAME_DURATION, framesCaminarDerecha);
//
//        TextureRegion[] framesIdle = {frames[0][0]};
//        animacionIdle = new Animation<>(1f, framesIdle);
//    }
//
//    public boolean mover(Direccion direccion, float deltaTime, TileMapRun tileMap) {
//        float nuevaX = x + direccion.dx * velocidad * deltaTime;
//        float nuevaY = y + direccion.dy * velocidad * deltaTime;
//
////        int tileX = (int) (nuevaX / tileMap.tileWidth);
////        int tileY = (int) (nuevaY / tileMap.tileHeight);
//
////        if (tileMap.dentroInside(tileX, tileY)) {
////            TileCell cell = tileMap.grid[tileX][tileY];
////            
////            if (cell != null && !cell.isBloqueado()) {
////                int oldTileX = (int) (x / tileMap.tileWidth);
////                int oldTileY = (int) (y / tileMap.tileHeight);
////                if (tileMap.dentroInside(oldTileX, oldTileY) && tileMap.grid[oldTileX][oldTileY] != null) {
////                    tileMap.grid[oldTileX][oldTileY].tieneJugador = false;
////                }
////                
////                x = nuevaX;
////                y = nuevaY;
////                bounds.setPosition(x, y);
////                
////                cell.tieneJugador = true;
////                
////                estaMoviendo = true;
////                direccionActual = direccion;
////                return true; 
////            }else{
////                estaMoviendo = false;
////                return false;
////            }
////        }
//        if (!colisionesMuro(nuevaX, nuevaY, tileMap)) {
//            x = nuevaX;
//            y = nuevaY;
//            bounds.setPosition(x, y);
//
//            estaMoviendo = true;
//            direccionActual = direccion;
//            return true;
//        }
//        estaMoviendo = false;
//        direccionActual = direccion;
//        return false;
//    }
//
//    private boolean colisionesMuro(float nuevaX, float nuevaY, TileMapRun tileMap) {
//        Rectangle futuro = new Rectangle(nuevaX, nuevaY, bounds.width, bounds.height);
//
//    int leftTile   = (int)(futuro.x / tileMap.tileWidth);
//    int rightTile  = (int)((futuro.x + futuro.width - 1) / tileMap.tileWidth);
//    int bottomTile = (int)(futuro.y / tileMap.tileHeight);
//    int topTile    = (int)((futuro.y + futuro.height - 1) / tileMap.tileHeight);
//
//    for (int tx = leftTile; tx <= rightTile; tx++) {
//        for (int ty = bottomTile; ty <= topTile; ty++) {
//            if (!tileMap.dentroInside(tx, ty)) return true; 
//            TileCell cell = tileMap.grid[tx][ty];
//            if (cell != null && cell.isBloqueado()) {
//                return true; 
//            }
//        }
//    }
//    return false;

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