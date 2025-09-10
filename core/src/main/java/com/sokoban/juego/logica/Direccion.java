/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.sokoban.juego.logica;

/**
 *
 * @author hnleo
 */
public enum Direccion {
    
    UP(0,1), DOWN(0,-1), LEFT(-1,0), RIGHT(1,0);
    
    public final int dx, dy;
    
    Direccion(int dx, int dy){
    this.dx = dx;
    this.dy = dy;
}
}
