/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.accounts;

/**
 *
 * @author unwir
 */
public class ProgresoPorNivel {

    private int nivelId;
    private boolean completado;
    private boolean desbloqueado;
    private int mejorPuntaje;
    private int menorCantidadMovimientos;
    private int vecesCompletado;
    private long tiempoMejorRecord; // en milisegundos

    public ProgresoPorNivel(int nivelId) {
        this.nivelId = nivelId;
        this.completado = false;
        this.desbloqueado = (nivelId == 1); // Solo el primer nivel está desbloqueado inicialmente
        this.mejorPuntaje = 0;
        this.menorCantidadMovimientos = Integer.MAX_VALUE;
        this.vecesCompletado = 0;
        this.tiempoMejorRecord = Long.MAX_VALUE;
    }

    public int calcularPuntaje(int movimientos, long tiempoEnMs) {
        // Sistema de puntaje basado en eficiencia
        int puntajeBase = 1000;
        int puntajeMovimientos = Math.max(0, puntajeBase - (movimientos * 10));
        int puntajeTiempo = Math.max(0, (int) (puntajeBase - (tiempoEnMs / 1000))); // -1 punto por segundo

        return puntajeMovimientos + puntajeTiempo;
    }

    public void actualizarRecord(int movimientos, long tiempoEnMs) {
        int nuevoPuntaje = calcularPuntaje(movimientos, tiempoEnMs);

        if (!completado) {
            // Primera vez completando el nivel
            completado = true;
            mejorPuntaje = nuevoPuntaje;
            menorCantidadMovimientos = movimientos;
            tiempoMejorRecord = tiempoEnMs;
        } else {
            // Actualizar si es mejor
            if (nuevoPuntaje > mejorPuntaje) {
                mejorPuntaje = nuevoPuntaje;
            }
            if (movimientos < menorCantidadMovimientos) {
                menorCantidadMovimientos = movimientos;
            }
            if (tiempoEnMs < tiempoMejorRecord) {
                tiempoMejorRecord = tiempoEnMs;
            }
        }

        vecesCompletado++;
    }

    public String getClasificacion() {
        if (!completado) {
            return "Sin completar";
        }

        if (mejorPuntaje >= 1800) {
            return "★★★ PERFECTO";
        }
        if (mejorPuntaje >= 1500) {
            return "★★☆ EXCELENTE";
        }
        if (mejorPuntaje >= 1200) {
            return "★☆☆ BUENO";
        }
        return "☆☆☆ COMPLETADO";
    }

    // Getters y Setters
    public int getNivelId() {
        return nivelId;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public boolean isDesbloqueado() {
        return desbloqueado;
    }

    public void setDesbloqueado(boolean desbloqueado) {
        this.desbloqueado = desbloqueado;
    }

    public int getMejorPuntaje() {
        return mejorPuntaje;
    }

    public void setMejorPuntaje(int mejorPuntaje) {
        this.mejorPuntaje = mejorPuntaje;
    }

    public int getMenorCantidadMovimientos() {
        return menorCantidadMovimientos == Integer.MAX_VALUE ? 0 : menorCantidadMovimientos;
    }

    public void setMenorCantidadMovimientos(int menorCantidadMovimientos) {
        this.menorCantidadMovimientos = menorCantidadMovimientos;
    }

    public int getVecesCompletado() {
        return vecesCompletado;
    }

    public void setVecesCompletado(int vecesCompletado) {
        this.vecesCompletado = vecesCompletado;
    }

    public long getTiempoMejorRecord() {
        return tiempoMejorRecord == Long.MAX_VALUE ? 0 : tiempoMejorRecord;
    }

    public void setTiempoMejorRecord(long tiempoMejorRecord) {
        this.tiempoMejorRecord = tiempoMejorRecord;
    }
}
