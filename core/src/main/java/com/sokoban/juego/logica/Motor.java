package com.sokoban.juego.logica;

import com.badlogic.gdx.graphics.Texture;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hnleo
 */
public class Motor {

    private Elemento[][] mapa;
    private int[][] layout;
    private Jugador jugador;
    private int filas, columnas;
    private Texture cajaImg, metaImg, sueloImg;

    public interface MotorListener {

        void onMovimientoRealizado();

        void onNivelCompletado();

        void onMovimientoInvalido();
    }

    private MotorListener listener;

    public Motor(Elemento[][] mapa, int[][] layout, Jugador jugador,
            int filas, int columnas, Texture cajaImg, Texture metaImg, Texture sueloImg) {
        this.mapa = mapa;
        this.layout = layout;
        this.jugador = jugador;
        this.filas = filas;
        this.columnas = columnas;
        this.cajaImg = cajaImg;
        this.metaImg = metaImg;
        this.sueloImg = sueloImg;
    }

    public void setListener(MotorListener listener) {
        this.listener = listener;
    }

    public boolean moverJugador(int dx, int dy) {
        if (jugador == null || jugador.estaMoviendose()) {
            return false;
        }

        int nx = jugador.x + dx;
        int ny = jugador.y + dy;

        if (nx < 0 || nx >= columnas || ny < 0 || ny >= filas) {
            notificarMovimientoInvalido();
            return false;
        }

        Elemento obj = mapa[ny][nx];

        if (obj instanceof Muro) {
            notificarMovimientoInvalido();
            return false;
        }

        if (obj instanceof Caja) {
            if (empujarCajas(nx, ny, dx, dy)) {
                jugador.moverCelda(nx, ny);
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
            jugador.moverCelda(nx, ny);
            notificarMovimientoRealizado();
            return true;
        }
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

            Caja cajaNueva = new Caja(nuevaX, nuevaY, cajaImg);
            mapa[nuevaY][nuevaX] = cajaNueva;

            restaurarElementoOriginal(cajaPos.x, cajaPos.y);
        }
    }

    private void restaurarElementoOriginal(int x, int y) {
        if (layout[y][x] == 3) { // Era un objetivo
            mapa[y][x] = new Objetivo(x, y, metaImg);
        } else {
            mapa[y][x] = new Terreno(x, y, sueloImg);
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
            return new Posicion(jugador.x, jugador.y);
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
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
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
