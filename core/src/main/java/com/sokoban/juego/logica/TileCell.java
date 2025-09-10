/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

/**
 *
 * @author hnleo
 */
public class TileCell {
    public final int x, y;
    public boolean solido, movible, objetivo;
    public String tipo;
    public boolean tieneCaja = false, tieneJugador = false;
    
    public TileCell(int x, int y, boolean solido, boolean movible, boolean objetivo, String tipo){
        this.x = x;
        this.y = y;
        this.solido = solido;
        this.movible = movible;
        this.objetivo = objetivo;
        this.tipo = tipo;
        
    }
    
    public boolean isBloqueado(){
        return solido || tieneCaja;
    }
    
}
