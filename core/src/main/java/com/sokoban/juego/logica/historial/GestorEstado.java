/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.historial;

import com.sokoban.juego.logica.Caja;
import com.sokoban.juego.logica.Elemento;
import com.sokoban.juego.logica.Jugador;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author hnleo
 */
public class GestorEstado {

    private Stack<EstadoJuego> historialEstados;
    private final int maxHistorial;
    private final ReentrantLock lock;

    public GestorEstado(int maxHistorial) {
        this.maxHistorial = maxHistorial;
        this.historialEstados = new Stack<>();
        this.lock = new ReentrantLock();
    }

    public GestorEstado() {
        this(50);
    }

    public void guardarEstado(Elemento[][] mapa, int[][] layout, Jugador jugador,
            int filas, int columnas, int movimientosActuales, int scoreActual,
            Jugador.DireccionMovimiento direccionMovimiento, boolean fueEmpuje) {

        lock.lock();
        try {

            EstadoJuego.PosicionElemento posJugador = new EstadoJuego.PosicionElemento(
                    jugador.getX(), jugador.getY(), 4
            );

            List<EstadoJuego.PosicionElemento> cajas = new ArrayList<>();
            for (int y = 0; y < filas; y++) {
                for (int x = 0; x < columnas; x++) {
                    if (mapa[y][x] instanceof Caja) {
                        Caja caja = (Caja) mapa[y][x];
                        boolean enObjetivo = layout[y][x] == 3;
                        cajas.add(new EstadoJuego.PosicionElemento(x, y, 2, enObjetivo));
                    }
                }
            }

            EstadoJuego estado = new EstadoJuego(posJugador, cajas, movimientosActuales,
                    scoreActual, direccionMovimiento, fueEmpuje);

            historialEstados.push(estado);

            if (historialEstados.size() > maxHistorial) {
                Stack<EstadoJuego> nuevoHistorial = new Stack<>();
                for (int i = 1; i < historialEstados.size(); i++) {
                    nuevoHistorial.push(historialEstados.get(i));
                }
                historialEstados = nuevoHistorial;
            }

        } finally {
            lock.unlock();
        }
    }

    public EstadoJuego obtenerEstadoAnterior() {
        lock.lock();
        try {
            if (!historialEstados.isEmpty()) {
                EstadoJuego estado = historialEstados.pop();

                return estado;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public boolean tieneEstadosAnteriores() {
        lock.lock();
        try {
            return !historialEstados.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public void limpiarHistorial() {
        lock.lock();
        try {
            historialEstados.clear();
        } finally {
            lock.unlock();
        }
    }

    public int getTamanoHistorial() {
        lock.lock();
        try {
            return historialEstados.size();
        } finally {
            lock.unlock();
        }
    }
}
