/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.historial;

import com.badlogic.gdx.graphics.Texture;
import com.sokoban.juego.logica.Caja;
import com.sokoban.juego.logica.Elemento;
import com.sokoban.juego.logica.Jugador;
import com.sokoban.juego.logica.Objetivo;
import com.sokoban.juego.logica.Terreno;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author hnleo
 */
public class ProcesoRegresar implements Runnable {

    private final GestorEstado gestorHistorial;
    private final Elemento[][] mapa;
    private final int[][] layout;
    private final Jugador jugador;
    private final int filas, columnas, tileSize;
    private final Texture cajaImg, cajaEnObjetivoImg, metaImg, sueloImg;

    private final AtomicBoolean procesandoUndo = new AtomicBoolean(false);
    private final AtomicBoolean detenerHilo = new AtomicBoolean(false);
    private final AtomicBoolean undoPendiente = new AtomicBoolean(false);

    private volatile UndoListener listener;
    private volatile int movimientosActuales;
    private volatile int scoreActual;

    public interface UndoListener {

        void onUndoCompletado(boolean exitoso);

        void onUndoIniciado();
    }

    public ProcesoRegresar(GestorEstado gestorHistorial, Elemento[][] mapa, int[][] layout,
            Jugador jugador, int filas, int columnas, int tileSize,
            Texture cajaImg, Texture cajaEnObjetivoImg, Texture metaImg, Texture sueloImg) {
        this.gestorHistorial = gestorHistorial;
        this.mapa = mapa;
        this.layout = layout;
        this.jugador = jugador;
        this.filas = filas;
        this.columnas = columnas;
        this.tileSize = tileSize;
        this.cajaImg = cajaImg;
        this.cajaEnObjetivoImg = cajaEnObjetivoImg;
        this.metaImg = metaImg;
        this.sueloImg = sueloImg;
    }

    public void setListener(UndoListener listener) {
        this.listener = listener;
    }

    public boolean solicitarUndo(int movimientosActuales, int scoreActual) {
        if (procesandoUndo.get()) {
            System.out.println("Regresar ya en proceso");
            return false;
        }

        if (!gestorHistorial.tieneEstadosAnteriores()) {
            System.out.println("No hay movimientos para deshacer");
            return false;
        }

        this.movimientosActuales = movimientosActuales;
        this.scoreActual = scoreActual;
        undoPendiente.set(true);

        return true;
    }

    public boolean estaProcesandoUndo() {
        return procesandoUndo.get();
    }

    public void detener() {
        detenerHilo.set(true);
    }

    @Override
    public void run() {

        while (!detenerHilo.get()) {
            try {
                if (undoPendiente.get()) {
                    procesarUndo();
                }

                Thread.sleep(16);

            } catch (InterruptedException e) {
                System.out.println("Hilo ProcesoRegresar interrumpido");
                break;
            } catch (Exception e) {
                System.err.println("Error en ProcesoRegresar: " + e.getMessage());
                e.printStackTrace();
                procesandoUndo.set(false);
                undoPendiente.set(false);
            }
        }

    }

    private void procesarUndo() {
        procesandoUndo.set(true);
        undoPendiente.set(false);

        try {
            if (listener != null) {
                listener.onUndoIniciado();
            }

            EstadoJuego estadoAnterior = gestorHistorial.obtenerEstadoAnterior();
            if (estadoAnterior == null) {
                if (listener != null) {
                    listener.onUndoCompletado(false);
                }
                return;
            }

            boolean exitoso = realizarUndoConAnimacion(estadoAnterior);

            if (listener != null) {
                listener.onUndoCompletado(exitoso);
            }

        } finally {
            procesandoUndo.set(false);
        }
    }

    private boolean realizarUndoConAnimacion(EstadoJuego estadoAnterior) {
        try {

            restaurarCajas(estadoAnterior.getPosicionesCajas());

            Jugador.DireccionMovimiento direccionInversa = estadoAnterior.getDireccionInversa();
            jugador.cambiarDireccion(direccionInversa);

            EstadoJuego.PosicionElemento posJugador = estadoAnterior.getPosicionJugador();

            if (estadoAnterior.getFueEmpuje()) {
                jugador.moverEmpujandoA(posJugador.x, posJugador.y);
            } else {
                jugador.moverA(posJugador.x, posJugador.y);
            }

            esperarFinAnimacion();

            return true;

        } catch (Exception e) {
            System.err.println("Error realizando undo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

   private void restaurarCajas(List<EstadoJuego.PosicionElemento> cajasAnteriores) {
    synchronized (mapa) {
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (mapa[y][x] instanceof Caja) {
                    if (layout[y][x] == 3) { // Si había una caja sobre un objetivo
                        mapa[y][x] = new Objetivo(x, y, metaImg);
                    } else { // Si estaba en suelo normal
                        mapa[y][x] = new Terreno(x, y, sueloImg);
                    }
                }
            }
        }
        for (EstadoJuego.PosicionElemento cajaPosicion : cajasAnteriores) {
            Caja caja;
            if (cajaEnObjetivoImg != null) {
                caja = new Caja(cajaPosicion.x, cajaPosicion.y, cajaImg, cajaEnObjetivoImg, tileSize);
            } else {
                caja = new Caja(cajaPosicion.x, cajaPosicion.y, cajaImg, tileSize);
            }
           
            mapa[cajaPosicion.y][cajaPosicion.x] = caja;
        }

        actualizarEstadoCajas();
    }
}

    private void actualizarEstadoCajas() {
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (mapa[y][x] instanceof Caja) {
                    Caja caja = (Caja) mapa[y][x];
                    boolean estaEnObjetivo = layout[y][x] == 3;
                    caja.setEstaEnObjetivo(estaEnObjetivo);
                }
            }
        }
    }

    private void esperarFinAnimacion() throws InterruptedException {
        int maxEspera = 5000;
        int tiempoEsperado = 0;

        while (jugador.estaMoviendose() && tiempoEsperado < maxEspera) {
            Thread.sleep(16);
            tiempoEsperado += 16;
        }

        if (tiempoEsperado >= maxEspera) {
        }

        Thread.sleep(100);
    }
}