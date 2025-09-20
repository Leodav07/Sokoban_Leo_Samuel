package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import com.sokoban.juego.logica.historial.GestorEstado;
import com.sokoban.juego.logica.historial.ProcesoRegresar;
import java.util.ArrayList;
import java.util.List;

public class Motor implements ProcesoRegresar.UndoListener {

    private Elemento[][] mapa;
    private int[][] layout;
    private Jugador jugador;
    private int filas, columnas;
    private int tileSize;
    private Texture cajaImg, metaImg, sueloImg;
    private Texture cajaEnObjetivoImg;
    
    private GestorEstado gestorHistorial;
    private ProcesoRegresar procesadorUndo;
    private Thread hiloUndo;

    public interface MotorListener {
        void onMovimientoRealizado();
        void onNivelCompletado();
        void onMovimientoInvalido();
        void onUndoRealizado();
        void onUndoIniciado(); 
    }

    private MotorListener listener;
    private volatile boolean undoEnProceso = false;

    public Motor(Elemento[][] mapa, int[][] layout, Jugador jugador,
            int filas, int columnas, Texture cajaImg, Texture metaImg, Texture sueloImg, int tileSize) {
        this.mapa = mapa;
        this.layout = layout;
        this.jugador = jugador;
        this.filas = filas;
        this.columnas = columnas;
        this.cajaImg = cajaImg;
        this.metaImg = metaImg;
        this.sueloImg = sueloImg;
        this.tileSize = tileSize;
        this.cajaEnObjetivoImg = cajaImg;
        
        inicializarSistemaUndo();
    }
    
    private void inicializarSistemaUndo() {
        this.gestorHistorial = new GestorEstado();
        
        this.procesadorUndo = new ProcesoRegresar(
            gestorHistorial, mapa, layout, jugador, filas, columnas, tileSize,
            cajaImg, cajaEnObjetivoImg, metaImg, sueloImg
        );
        
        this.procesadorUndo.setListener(this);
        
        this.hiloUndo = new Thread(procesadorUndo, "UndoProcessor");
        this.hiloUndo.setDaemon(true); 
        this.hiloUndo.start();
        
    }

    public void setCajaEnObjetivoTexture(Texture cajaEnObjetivoImg) {
        this.cajaEnObjetivoImg = cajaEnObjetivoImg;
    }

    public void setListener(MotorListener listener) {
        this.listener = listener;
    }
    
    
    private void guardarEstadoActual(int movimientosActuales, int scoreActual, 
                                   Jugador.DireccionMovimiento direccion, boolean fueEmpuje) {
        gestorHistorial.guardarEstado(mapa, layout, jugador, filas, columnas, 
                                     movimientosActuales, scoreActual, direccion, fueEmpuje);
    }
    
    public boolean realizarUndo(int movimientosActuales, int scoreActual) {
        if (undoEnProceso) {
            return false;
        }
        
        if (jugador.estaMoviendose()) {
            return false;
        }
        
        return procesadorUndo.solicitarUndo(movimientosActuales, scoreActual);
    }
    
    @Override
    public void onUndoIniciado() {
        undoEnProceso = true;
        
        if (listener != null) {
            listener.onUndoIniciado();
        }
    }
    
    @Override
    public void onUndoCompletado(boolean exitoso) {
        undoEnProceso = false;
        
        if (exitoso && listener != null) {
            listener.onUndoRealizado();
        }
    }

    public boolean moverJugador(int dx, int dy, int movimientosActuales, int scoreActual) {
        if (undoEnProceso) {
            return false;
        }
        
        if (jugador == null || jugador.estaMoviendose()) {
            return false;
        }
        
        int nuevoX = jugador.getX() + dx;
        int nuevoY = jugador.getY() + dy;

        if (nuevoX < 0 || nuevoX >= columnas || nuevoY < 0 || nuevoY >= filas) {
            notificarMovimientoInvalido();
            return false;
        }

        Jugador.DireccionMovimiento direccion;
        if (dx > 0) direccion = Jugador.DireccionMovimiento.DERECHA;
        else if (dx < 0) direccion = Jugador.DireccionMovimiento.IZQUIERDA;
        else if (dy > 0) direccion = Jugador.DireccionMovimiento.ARRIBA;
        else direccion = Jugador.DireccionMovimiento.ABAJO;

        Elemento obj = mapa[nuevoY][nuevoX];

        if (obj instanceof Muro) {
            notificarMovimientoInvalido();
            return false;
        }

        boolean fueEmpuje = obj instanceof Caja;
        
        guardarEstadoActual(movimientosActuales, scoreActual, direccion, fueEmpuje);

        if (fueEmpuje) {
            if (empujarCajas(nuevoX, nuevoY, dx, dy)) {
                jugador.moverEmpujandoA(nuevoX, nuevoY);
                notificarMovimientoRealizado();

                if (nivelCompletado()) {
                    notificarNivelCompletado();
                }

                return true;
            } else {
                notificarMovimientoInvalido();
                return false;
            }
        } else {
            jugador.moverA(nuevoX, nuevoY);
            notificarMovimientoRealizado();
            return true;
        }
    }
    
    public boolean moverJugador(int dx, int dy) {
        return moverJugador(dx, dy, 0, 0);
    }

    private boolean empujarCajas(int inicialX, int inicialY, int dx, int dy) {
        List<Posicion> cajas = new ArrayList<>();
        int checkX = inicialX;
        int checkY = inicialY;
        Elemento elemento = mapa[checkY][checkX];
        if (elemento instanceof Caja) {
            cajas.add(new Posicion(checkX, checkY));
            checkX += dx;
            checkY += dy;
            if (checkX >= 0 && checkX < columnas && checkY >= 0 && checkY < filas) {
                Elemento siguienteElemento = mapa[checkY][checkX];
                if (siguienteElemento instanceof Caja) {
                    return false;
                }
            }
        } else {
            return false;
        }
        if (!sePuedenMoverCajas(cajas, dx, dy)) {
            return false;
        }
        moverCajas(cajas, dx, dy);
        return true;
    }

    private boolean sePuedenMoverCajas(List<Posicion> cajas, int dx, int dy) {
        for (int i = cajas.size() - 1; i >= 0; i--) {
            Posicion caja = cajas.get(i);
            int nuevaX = caja.x + dx;
            int nuevaY = caja.y + dy;

            if (nuevaX < 0 || nuevaX >= columnas || nuevaY < 0 || nuevaY >= filas) {
                return false;
            }

            Elemento destino = mapa[nuevaY][nuevaX];

            if (i == cajas.size() - 1) {
                if (!(destino instanceof Terreno || destino instanceof Objetivo)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void moverCajas(List<Posicion> cajas, int dx, int dy) {
        for (int i = cajas.size() - 1; i >= 0; i--) {
            Posicion cajaPos = cajas.get(i);
            int nuevaX = cajaPos.x + dx;
            int nuevaY = cajaPos.y + dy;

            Caja cajaNueva;
            if (cajaEnObjetivoImg != null && !cajaEnObjetivoImg.equals(cajaImg)) {
                cajaNueva = new Caja(nuevaX, nuevaY, cajaImg, cajaEnObjetivoImg, tileSize);
            } else {
                cajaNueva = new Caja(nuevaX, nuevaY, cajaImg, tileSize);
            }
            
            boolean nuevaPosEsObjetivo = layout[nuevaY][nuevaX] == 3;
            cajaNueva.setEstaEnObjetivo(nuevaPosEsObjetivo);
            
            mapa[nuevaY][nuevaX] = cajaNueva;

            restaurarElementoOriginal(cajaPos.x, cajaPos.y);
        }
        
        actualizarEstadoCajas();
    }

    private void actualizarEstadoCajas() {
        synchronized (mapa) {
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
    }

    private void restaurarElementoOriginal(int x, int y) {
        if (layout[y][x] == 3) {
            mapa[y][x] = new Objetivo(x, y, metaImg);
        } else {
            mapa[y][x] = new Terreno(x, y, sueloImg);
        }
    }
    
   
    public void limpiarHistorial() {
        gestorHistorial.limpiarHistorial();
    }
    
   
    public boolean puedeHacerUndo() {
        return gestorHistorial.tieneEstadosAnteriores() && !undoEnProceso;
    }
    
    
    public boolean estaEjecutandoUndo() {
        return undoEnProceso;
    }
    
   
    public void finalizarSistemaUndo() {
        if (procesadorUndo != null) {
            procesadorUndo.detener();
        }
        
        if (hiloUndo != null) {
            try {
                hiloUndo.join(1000); 
            } catch (InterruptedException e) {
                System.err.println("Error esperando finalización del hilo regresar: " + e.getMessage());
            }
        }
        
    }

    public boolean nivelCompletado() {
        int cajasEnObjetivo = 0;
        int objetivosTotales = 0;

        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (layout[y][x] == 3) {
                    objetivosTotales++;
                    if (mapa[y][x] instanceof Caja) {
                        cajasEnObjetivo++;
                    }
                }
            }
        }

        return objetivosTotales > 0 && cajasEnObjetivo == objetivosTotales;
    }

    public int getCajasEnObjetivo() {
        int count = 0;
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (layout[y][x] == 3 && mapa[y][x] instanceof Caja) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getObjetivosTotales() {
        int count = 0;
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (layout[y][x] == 3) {
                    count++;
                }
            }
        }
        return count;
    }

    public Posicion getPosicionJugador() {
        if (jugador != null) {
            return new Posicion(jugador.getX(), jugador.getY());
        }
        return null;
    }

    private void notificarMovimientoRealizado() {
        if (listener != null) {
            listener.onMovimientoRealizado();
        }
    }

    private void notificarNivelCompletado() {
        if (listener != null) {
            listener.onNivelCompletado();
        }
    }

    private void notificarMovimientoInvalido() {
        if (listener != null) {
            listener.onMovimientoInvalido();
        }
    }

    public static class Posicion {
        public final int x, y;

        public Posicion(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Posicion posicion = (Posicion) obj;
            return x == posicion.x && y == posicion.y;
        }

        @Override
        public int hashCode() {
            return x * 1000 + y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}