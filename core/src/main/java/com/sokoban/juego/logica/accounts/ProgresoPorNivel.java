/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.accounts;

import com.sokoban.juego.niveles.ConfigNiveles;

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
    private long tiempoMejorRecord;

    public ProgresoPorNivel(int nivelId) {
        this.nivelId = nivelId;
        this.completado = false;
        this.desbloqueado = (nivelId == 1); // Solo el primer nivel está desbloqueado inicialmente (standard)
        this.mejorPuntaje = 0;
        this.menorCantidadMovimientos = Integer.MAX_VALUE;
        this.vecesCompletado = 0;
        this.tiempoMejorRecord = Long.MAX_VALUE;
    }

    public int calcularPuntaje(int movimientos) {
        int puntajeBase = 10000;
        // <<--- CORRECCIÓN: Usamos tu método existente --->>
        int movimientosPar = ConfigNiveles.getMovimientosObjetivo(this.nivelId);

        if (movimientos > movimientosPar) {
            return puntajeBase;
        }

        int movimientosAhorrados = movimientosPar - movimientos;
        int bonificacion = movimientosAhorrados * 150;

        return puntajeBase + bonificacion;
    }

    public void actualizarRecord(int movimientos, long tiempoEnMs) {
        int nuevoPuntaje = calcularPuntaje(movimientos);

        if (!completado) {
            completado = true;
            mejorPuntaje = nuevoPuntaje;
            menorCantidadMovimientos = movimientos;
            tiempoMejorRecord = tiempoEnMs;
        } else {
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

    public int getEstrellasGanadas() {
        if (!completado) {
            return 0;
        }

        int estrellas = 1;

        int movimientosPar = ConfigNiveles.getMovimientosObjetivo(this.nivelId);
        long tiempoObjetivo = ConfigNiveles.getTiempoObjetivo(this.nivelId);

        if (menorCantidadMovimientos <= movimientosPar) {
            estrellas++;
        }

        if (tiempoMejorRecord <= tiempoObjetivo) {
            estrellas++;
        }

        return estrellas;
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
