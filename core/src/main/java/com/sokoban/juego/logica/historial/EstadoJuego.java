/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.historial;

import com.sokoban.juego.logica.Jugador;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hnleo
 */
public class EstadoJuego {

    public static class PosicionElemento {

        public final int x, y;
        public final int tipo;
        public final boolean estaEnObjetivo;

        public PosicionElemento(int x, int y, int tipo, boolean estaEnObjetivo) {
            this.x = x;
            this.y = y;
            this.tipo = tipo;
            this.estaEnObjetivo = estaEnObjetivo;
        }

        public PosicionElemento(int x, int y, int tipo) {
            this(x, y, tipo, false);
        }
    }

    private final PosicionElemento posicionJugador;
    private final List<PosicionElemento> posicionesCajas;
    private final int movimientosRealizados;
    private final int scoreAnterior;
    private final Jugador.DireccionMovimiento direccionMovimiento;
    private final boolean fueEmpuje; 

    public EstadoJuego(PosicionElemento posicionJugador, List<PosicionElemento> posicionesCajas,
            int movimientosRealizados, int scoreAnterior,
            Jugador.DireccionMovimiento direccionMovimiento, boolean fueEmpuje) {
        this.posicionJugador = posicionJugador;
        this.posicionesCajas = new ArrayList<>(posicionesCajas);
        this.movimientosRealizados = movimientosRealizados;
        this.scoreAnterior = scoreAnterior;
        this.direccionMovimiento = direccionMovimiento;
        this.fueEmpuje = fueEmpuje;
    }

    public Jugador.DireccionMovimiento getDireccionInversa() {
        switch (direccionMovimiento) {
            case ARRIBA:
                return Jugador.DireccionMovimiento.ARRIBA;
            case ABAJO:
                return Jugador.DireccionMovimiento.ABAJO;
            case IZQUIERDA:
                return Jugador.DireccionMovimiento.IZQUIERDA;
            case DERECHA:
                return Jugador.DireccionMovimiento.DERECHA;
            default:
                return Jugador.DireccionMovimiento.ARRIBA;
        }
    }

    public PosicionElemento getPosicionJugador() {
        return posicionJugador;
    }

    public List<PosicionElemento> getPosicionesCajas() {
        return new ArrayList<>(posicionesCajas);
    }

    public int getMovimientosRealizados() {
        return movimientosRealizados;
    }

    public int getScoreAnterior() {
        return scoreAnterior;
    }

    public Jugador.DireccionMovimiento getDireccionMovimiento() {
        return direccionMovimiento;
    }

    public boolean getFueEmpuje() {
        return fueEmpuje;
    }
}
