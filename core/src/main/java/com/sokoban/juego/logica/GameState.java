package com.sokoban.juego.logica;

import com.sokoban.juego.logica.accounts.GestorProgreso;
import java.util.Scanner;

public class GameState {
     private enum Elementos {
        VACIO, META,
        CAJA, MURO, JUGADOR,
        CAJAMETA, JUGADORMETA
    }

    private Elementos[][] grid;
    private int numCajasEnMeta = 0;
    private int numMetasTotales = 0;
    private int ancho, largo;
    private int playerY, playerX;
    
    // Nuevas variables para tracking
    private int contadorMovimientos = 0;
    private long tiempoInicio;
    private int nivelActual;
    private GestorProgreso gestorProgreso;

    public GameState(String[] layout, int nivelId) {
        this.nivelActual = nivelId;
        this.gestorProgreso = GestorProgreso.getInstancia();
        this.tiempoInicio = System.currentTimeMillis();
        this.contadorMovimientos = 0;
        
        this.largo = layout.length;
        this.ancho = layout[0].length();
        this.grid = new Elementos[largo][ancho];

        for (int y = 0; y < largo; y++) {
            for (int x = 0; x < ancho; x++) {
                char simbolo = layout[y].charAt(x);
                switch (simbolo) {
                    case '#': grid[y][x] = Elementos.MURO; break;
                    case ' ': grid[y][x] = Elementos.VACIO; break;
                    case '.':
                        grid[y][x] = Elementos.META;
                        numMetasTotales++;
                        break;
                    case ',': grid[y][x] = Elementos.CAJA; break;
                    case '@':
                        grid[y][x] = Elementos.JUGADOR;
                        this.playerX = x;
                        this.playerY = y;
                        break;
                    case '*':
                        grid[y][x] = Elementos.CAJAMETA;
                        numMetasTotales++;
                        numCajasEnMeta++;
                        break;
                    case '!':
                        grid[y][x] = Elementos.JUGADORMETA;
                        numMetasTotales++;
                        this.playerX = x;
                        this.playerY = y;
                        break;
                    default: grid[y][x] = Elementos.VACIO; break;
                }
            }
        }
    }

    public void moverPlayer(int dx, int dy) {
        int targetX = playerX + dx;
        int targetY = playerY + dy;

        if (targetY < 0 || targetY >= largo || targetX < 0 || targetX >= ancho || grid[targetY][targetX] == Elementos.MURO) {
            return;
        }

        Elementos destino = grid[targetY][targetX];

        if (destino == Elementos.VACIO || destino == Elementos.META) {
            moverCelda(playerY, playerX, targetY, targetX);
            this.playerX = targetX;
            this.playerY = targetY;
            contadorMovimientos++; // Incrementar contador
            return;
        }

        if (destino == Elementos.CAJA || destino == Elementos.CAJAMETA) {
            int cajaXA = targetX + dx;
            int cajaYA = targetY + dy;

            if (cajaYA < 0 || cajaYA >= largo || cajaXA < 0 || cajaXA >= ancho) {
                return;
            }

            Elementos celdaCajaA = grid[cajaYA][cajaXA];
            if (celdaCajaA == Elementos.VACIO || celdaCajaA == Elementos.META) {
                moverCelda(targetY, targetX, cajaYA, cajaXA); 
                moverCelda(playerY, playerX, targetY, targetX); 
                this.playerX = targetX;
                this.playerY = targetY;
                contadorMovimientos++; // Incrementar contador
            }
        }
    }

    private void moverCelda(int origenY, int origenX, int destinoY, int destinoX) {
        Elementos entidad = grid[origenY][origenX];
        Elementos celdaDestino = grid[destinoY][destinoX];

        if (entidad == Elementos.JUGADORMETA || entidad == Elementos.CAJAMETA) {
            grid[origenY][origenX] = Elementos.META;
        } else {
            grid[origenY][origenX] = Elementos.VACIO;
        }

        boolean esJugador = (entidad == Elementos.JUGADOR || entidad == Elementos.JUGADORMETA);
        if (celdaDestino == Elementos.META) {
            grid[destinoY][destinoX] = esJugador ? Elementos.JUGADORMETA : Elementos.CAJAMETA;
        } else {
            grid[destinoY][destinoX] = esJugador ? Elementos.JUGADOR : Elementos.CAJA;
        }

        actualizarMetas();
    }

    private void actualizarMetas() {
        numCajasEnMeta = 0;
        for (int y = 0; y < largo; y++) {
            for (int x = 0; x < ancho; x++) {
                if (grid[y][x] == Elementos.CAJAMETA) {
                    numCajasEnMeta++;
                }
            }
        }
    }

    public void imprimirGame() {
        System.out.println("\nNivel " + nivelActual + " | Movimientos: " + contadorMovimientos + 
                          " | Tiempo: " + formatearTiempo(getTiempoTranscurrido()));
        
        for (int y = 0; y < largo; y++) {
            for (int x = 0; x < ancho; x++) {
                char simbolo = ' ';
                switch (grid[y][x]) {
                    case MURO: simbolo = '#'; break;
                    case VACIO: simbolo = ' '; break;
                    case META: simbolo = '.'; break;
                    case CAJA: simbolo = ','; break;
                    case JUGADOR: simbolo = '@'; break;
                    case CAJAMETA: simbolo = '*'; break;
                    case JUGADORMETA: simbolo = '!'; break;
                }
                System.out.print(simbolo);
            }
            System.out.println();
        }
        System.out.println("Cajas en meta: " + numCajasEnMeta + "/" + numMetasTotales);
    }

    public boolean verificarVictoria() {
        boolean victoria = numMetasTotales > 0 && numCajasEnMeta == numMetasTotales;
        
        if (victoria) {
            // Registrar la victoria en el sistema de progreso
            long tiempoFinal = getTiempoTranscurrido();
            gestorProgreso.completarNivel(nivelActual, contadorMovimientos, tiempoFinal);
        }
        
        return victoria;
    }
    
    public long getTiempoTranscurrido() {
        return System.currentTimeMillis() - tiempoInicio;
    }
    
    private String formatearTiempo(long tiempoMs) {
        long segundos = tiempoMs / 1000;
        long minutos = segundos / 60;
        segundos = segundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
    
    // Getters adicionales
    public int getContadorMovimientos() { return contadorMovimientos; }
    public int getNivelActual() { return nivelActual; }
}