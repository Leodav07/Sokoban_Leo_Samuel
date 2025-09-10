/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

/**
 *
 * @author hnleo
 */
public class TileMapRun {
    public final int width, height, tileWidth, tileHeight;
    public final TileCell[][] grid;
    
    public TileMapRun(int width, int height, int tileWidth, int tileHeight){
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        grid = new TileCell[width][height];
    }
    
    public boolean dentroInside(int x, int y){
        return x>=0 && y>=0 && x<width && y<height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public TileCell[][] getGrid() {
        return grid;
    }
    
    
}
