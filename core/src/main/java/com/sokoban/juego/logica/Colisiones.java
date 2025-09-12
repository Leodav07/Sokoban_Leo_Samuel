/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

import com.sokoban.juego.logica.Mapas.MapaBase;

/**
 *
 * @author hnleo
 */
public class Colisiones implements Runnable {
    private MapaBase mapa;
    private boolean corriendo;
    
    public Colisiones(MapaBase mapa){
        this.mapa = mapa;
        this.corriendo = true;
    }

    @Override
    public void run() {
        while(corriendo){
            synchronized(mapa){
                if (mapa.getGestorPausa() == null || !mapa.getGestorPausa().estaPausado()) {
                    mapa.verificarTeclas();
                }
            }
            try{
                Thread.sleep(8);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    
    public void detener(){
        corriendo = false;
    }
}
