/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Color;

/**
 *
 * @author unwir
 */
public class Nivel {
    public static final int BLOQUEADO = 0;
    public static final int DISPONIBLE = 1;
    public static final int SELECCIONADO = 2;
    public static final int COMPLETADO = 3;
    
    private int id;
    private String nombre;
    private float x, y;
    private int estado;
    private boolean esBoss;

    public Nivel(int id, String nombre, float x, float y) {
        this.id = id;
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.estado = (id == 1) ? DISPONIBLE : BLOQUEADO;
        this.esBoss = (id == 7);
    }
    
    public Color getColor(){
        switch(estado){
            case 0:
              return Color.GRAY;   
            case 1:
              return Color.CYAN;  
            case 2:
              return Color.GREEN;      
            case 3:
              return Color.YELLOW;             
            default:
              return Color.WHITE;  
        
        }
    
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
    
    public boolean puedeJugar(){
        return (estado == DISPONIBLE) || (estado == COMPLETADO);       
    }
    
     public void completar(){
        if (estado == SELECCIONADO ||estado == DISPONIBLE){
            estado = COMPLETADO; 
        }      
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    
    
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getEstado() {
        return estado;
    }

    public boolean esBoss() {
        return esBoss;
    } 
    
}
