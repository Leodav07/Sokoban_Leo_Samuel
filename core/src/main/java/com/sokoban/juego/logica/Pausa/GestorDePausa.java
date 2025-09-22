package com.sokoban.juego.logica.Pausa;

import com.sokoban.juego.logica.Pausa.EstadoJuego;

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
    
    // Método añadido para manejar el resize
    public void resize(int width, int height) {
        menuPausa.resize(width, height);
    }
    
    public void dispose() {
        menuPausa.dispose();
    }
}