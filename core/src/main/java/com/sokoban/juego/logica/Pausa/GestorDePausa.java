/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.Pausa;
import com.sokoban.juego.logica.Pausa.EstadoJuego;

/**
 *
 * @author hnleo
 */
public class GestorDePausa {
    private EstadoJuego estadoAnterior;
    private EstadoJuego estadoActual;
    private MenuPausa menuPausa;
    private long tiempoPausaInicio;
    private long tiempoTotalPausado;
    
    public GestorDePausa() {
        this.estadoActual = EstadoJuego.JUGANDO;
        this.estadoAnterior = EstadoJuego.JUGANDO;
        this.menuPausa = new MenuPausa();
        this.tiempoTotalPausado = 0;
    }
    
    public void setMenuPausaListener(MenuPausaListener listener) {
        menuPausa.setListener(listener);
    }
    
    public void pausar() {
        if (estadoActual == EstadoJuego.JUGANDO) {
            estadoAnterior = estadoActual;
            estadoActual = EstadoJuego.PAUSADO;
            tiempoPausaInicio = System.currentTimeMillis();
            menuPausa.mostrar();
        }
    }
    
    public void reanudar() {
        if (estadoActual == EstadoJuego.PAUSADO) {
            tiempoTotalPausado += System.currentTimeMillis() - tiempoPausaInicio;
            estadoActual = estadoAnterior;
            menuPausa.ocultar();
        }
    }
    
    public void cambiarEstado(EstadoJuego nuevoEstado) {
        estadoAnterior = estadoActual;
        estadoActual = nuevoEstado;
        
        if (nuevoEstado != EstadoJuego.PAUSADO) {
            menuPausa.ocultar();
        }
    }
    
    public boolean estaPausado() {
        return estadoActual == EstadoJuego.PAUSADO;
    }
    
    public EstadoJuego getEstadoActual() {
        return estadoActual;
    }
    
    public MenuPausa getMenuPausa() {
        return menuPausa;
    }
    
    public long getTiempoTotalPausado() {
        long tiempoActual = tiempoTotalPausado;
        if (estadoActual == EstadoJuego.PAUSADO) {
            tiempoActual += System.currentTimeMillis() - tiempoPausaInicio;
        }
        return tiempoActual;
    }
    
    public void reiniciarTiempoPausa() {
        tiempoTotalPausado = 0;
        tiempoPausaInicio = 0;
    }
    
    public void manejarInput() {
        menuPausa.manejarInput();
    }
    
    public void dispose() {
        menuPausa.dispose();
    }
}
