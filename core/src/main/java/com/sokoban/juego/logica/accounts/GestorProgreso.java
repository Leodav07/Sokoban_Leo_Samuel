/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica.accounts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sokoban.juego.logica.GestorUsuarios;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author unwir
 */
public class GestorProgreso {
   
    private static GestorProgreso instancia;
    private Map<Integer, ProgresoPorNivel> progresoPorNivel;
    private int puntajeTotalAcumulado;
    private int totalMovimientosRealizados;
    private long tiempoTotalJugado;
    
    private GestorProgreso() {
        progresoPorNivel = new HashMap<>();
        inicializarNiveles();
        puntajeTotalAcumulado = 0;
        totalMovimientosRealizados = 0;
        tiempoTotalJugado = 0;
    }
    
    public static GestorProgreso getInstancia() {
        if (instancia == null) {
            instancia = new GestorProgreso();
        }
        return instancia;
    }
    
    private void inicializarNiveles() {
        for (int i = 1; i <= 7; i++) {
            progresoPorNivel.put(i, new ProgresoPorNivel(i));
        }
    }
    
    public void completarNivel(int nivelId, int movimientos, long tiempoEnMs) {
        ProgresoPorNivel progreso = progresoPorNivel.get(nivelId);
        if (progreso != null) {
            int puntajeAnterior = progreso.getMejorPuntaje();
            progreso.actualizarRecord(movimientos, tiempoEnMs);
            int nuevoPuntaje = progreso.getMejorPuntaje();
            
            if (nuevoPuntaje > puntajeAnterior) {
                puntajeTotalAcumulado += (nuevoPuntaje - puntajeAnterior);
            }
            
            totalMovimientosRealizados += movimientos;
            tiempoTotalJugado += tiempoEnMs;
            
            if (nivelId < 7) {
                ProgresoPorNivel siguienteNivel = progresoPorNivel.get(nivelId + 1);
                if (siguienteNivel != null && !siguienteNivel.isDesbloqueado()) {
                    siguienteNivel.setDesbloqueado(true);
                    System.out.println("¡Nivel " + (nivelId + 1) + " desbloqueado!");
                }
            }
            
            guardarProgreso();
            mostrarResultadoNivel(nivelId, movimientos, tiempoEnMs);
        }
    }
    
    private void mostrarResultadoNivel(int nivelId, int movimientos, long tiempoMs) {
        ProgresoPorNivel progreso = progresoPorNivel.get(nivelId);
        if (progreso != null) {
            System.out.println("\n¡NIVEL " + nivelId + " COMPLETADO!");
            System.out.println("Movimientos realizados: " + movimientos);
            System.out.println("Tiempo: " + formatearTiempo(tiempoMs));
            System.out.println("Puntaje obtenido: " + progreso.calcularPuntaje(movimientos, tiempoMs));
            System.out.println("Mejor puntaje: " + progreso.getMejorPuntaje());
            System.out.println("Clasificación: " + progreso.getClasificacion());
            
            if (movimientos == progreso.getMenorCantidadMovimientos()) {
                System.out.println("¡NUEVO RÉCORD DE MOVIMIENTOS!");
            }
            if (tiempoMs == progreso.getTiempoMejorRecord()) {
                System.out.println("¡NUEVO RÉCORD DE TIEMPO!");
            }
        }
    }
    
    public boolean isNivelDesbloqueado(int nivelId) {
        ProgresoPorNivel progreso = progresoPorNivel.get(nivelId);
        return progreso != null && progreso.isDesbloqueado();
    }
    
    public boolean isNivelCompletado(int nivelId) {
        ProgresoPorNivel progreso = progresoPorNivel.get(nivelId);
        return progreso != null && progreso.isCompletado();
    }
    
    public int getPuntajeNivel(int nivelId) {
        ProgresoPorNivel progreso = progresoPorNivel.get(nivelId);
        return progreso != null ? progreso.getMejorPuntaje() : 0;
    }
    
    public void guardarProgreso() {
        Usuario usuario = GestorUsuarios.usuarioActual;
        if (usuario == null) {
            System.err.println("No hay usuario logueado para guardar progreso");
            return;
        }
        
        try {
            FileHandle carpetaUsuario = Gdx.files.local("users/" + usuario.getUsername());
            if (!carpetaUsuario.exists()) {
                carpetaUsuario.mkdirs();
            }
            
            FileHandle archivoProgreso = carpetaUsuario.child("progreso.dat");
            File file = archivoProgreso.file();
            
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                // Escribir datos globales
                raf.writeInt(puntajeTotalAcumulado);
                raf.writeInt(totalMovimientosRealizados);
                raf.writeLong(tiempoTotalJugado);
                
                // Escribir progreso de cada nivel
                for (int i = 1; i <= 7; i++) {
                    ProgresoPorNivel progreso = progresoPorNivel.get(i);
                    if (progreso != null) {
                        raf.writeInt(progreso.getNivelId());
                        raf.writeBoolean(progreso.isCompletado());
                        raf.writeBoolean(progreso.isDesbloqueado());
                        raf.writeInt(progreso.getMejorPuntaje());
                        raf.writeInt(progreso.getMenorCantidadMovimientos());
                        raf.writeInt(progreso.getVecesCompletado());
                        raf.writeLong(progreso.getTiempoMejorRecord());
                    }
                }
            }
            
            System.out.println("Progreso guardado para " + usuario.getUsername());
            
        } catch (IOException e) {
            System.err.println("Error guardando progreso: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void cargarProgreso() {
        Usuario usuario = GestorUsuarios.usuarioActual;
        if (usuario == null) {
            System.err.println("No hay usuario logueado para cargar progreso");
            return;
        }
        
        FileHandle archivoProgreso = Gdx.files.local("users/" + usuario.getUsername() + "/progreso.dat");
        if (!archivoProgreso.exists()) {
            System.out.println("No existe progreso previo para " + usuario.getUsername());
            return;
        }
        
        try {
            File file = archivoProgreso.file();
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                // Leer datos globales
                puntajeTotalAcumulado = raf.readInt();
                totalMovimientosRealizados = raf.readInt();
                tiempoTotalJugado = raf.readLong();
                
                // Leer progreso de cada nivel
                for (int i = 1; i <= 7; i++) {
                    int nivelId = raf.readInt();
                    boolean completado = raf.readBoolean();
                    boolean desbloqueado = raf.readBoolean();
                    int mejorPuntaje = raf.readInt();
                    int menorMovimientos = raf.readInt();
                    int vecesCompletado = raf.readInt();
                    long mejorTiempo = raf.readLong();
                    
                    ProgresoPorNivel progreso = progresoPorNivel.get(nivelId);
                    if (progreso != null) {
                        progreso.setCompletado(completado);
                        progreso.setDesbloqueado(desbloqueado);
                        progreso.setMejorPuntaje(mejorPuntaje);
                        progreso.setMenorCantidadMovimientos(menorMovimientos);
                        progreso.setVecesCompletado(vecesCompletado);
                        progreso.setTiempoMejorRecord(mejorTiempo);
                    }
                }
            }
            
            System.out.println("Progreso cargado para " + usuario.getUsername());
            
        } catch (IOException e) {
            System.err.println("Error cargando progreso: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void mostrarEstadisticas() {
        Usuario usuario = GestorUsuarios.usuarioActual;
        if (usuario == null) {
            System.out.println("No hay usuario logueado");
            return;
        }
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ESTADÍSTICAS DE " + usuario.getNombreCompleto().toUpperCase());
        System.out.println("Usuario: " + usuario.getUsername());
        System.out.println("=".repeat(50));
        
        System.out.println("RESUMEN GENERAL:");
        System.out.println("Puntaje Total: " + puntajeTotalAcumulado);
        System.out.println("Movimientos Totales: " + totalMovimientosRealizados);
        System.out.println("Tiempo Total Jugado: " + formatearTiempo(tiempoTotalJugado));
        
        int nivelesCompletados = 0;
        int nivelesDesbloqueados = 0;
        
        System.out.println("\nPROGRESO POR NIVEL:");
        for (int i = 1; i <= 7; i++) {
            ProgresoPorNivel progreso = progresoPorNivel.get(i);
            if (progreso != null) {
                if (progreso.isDesbloqueado()) nivelesDesbloqueados++;
                if (progreso.isCompletado()) nivelesCompletados++;
                
                String estado = "BLOQUEADO";
                if (progreso.isDesbloqueado()) {
                    estado = progreso.isCompletado() ? "COMPLETADO" : "DISPONIBLE";
                }
                
                System.out.println("Nivel " + i + ": " + estado);
                if (progreso.isCompletado()) {
                    System.out.println("  └─ Mejor Puntaje: " + progreso.getMejorPuntaje());
                    System.out.println("  └─ Menos Movimientos: " + progreso.getMenorCantidadMovimientos());
                    System.out.println("  └─ Mejor Tiempo: " + formatearTiempo(progreso.getTiempoMejorRecord()));
                    System.out.println("  └─ Clasificación: " + progreso.getClasificacion());
                    System.out.println("  └─ Veces Completado: " + progreso.getVecesCompletado());
                }
                System.out.println();
            }
        }
        
        System.out.println("PROGRESO GENERAL:");
        System.out.println("Niveles Desbloqueados: " + nivelesDesbloqueados + "/7");
        System.out.println("Niveles Completados: " + nivelesCompletados + "/7");
        
        if (nivelesCompletados == 7) {
            System.out.println("¡FELICIDADES! ¡HAS COMPLETADO TODOS LOS NIVELES!");
        }
        
        System.out.println("=".repeat(50));
    }
    
    public void resetearProgreso() {
        progresoPorNivel.clear();
        inicializarNiveles();
        puntajeTotalAcumulado = 0;
        totalMovimientosRealizados = 0;
        tiempoTotalJugado = 0;
        
        Usuario usuario = GestorUsuarios.usuarioActual;
        if (usuario != null) {
            FileHandle archivoProgreso = Gdx.files.local("users/" + usuario.getUsername() + "/progreso.dat");
            if (archivoProgreso.exists()) {
                archivoProgreso.delete();
            }
            System.out.println("Progreso reseteado para " + usuario.getUsername());
        }
    }
    
    private String formatearTiempo(long tiempoMs) {
        if (tiempoMs == 0 || tiempoMs == Long.MAX_VALUE) return "00:00";
        
        long segundos = tiempoMs / 1000;
        long minutos = segundos / 60;
        segundos = segundos % 60;
        
        return String.format("%02d:%02d", minutos, segundos);
    }
    
    public int getPuntajeTotalAcumulado() {
        return puntajeTotalAcumulado; 
    }
    public int getTotalMovimientosRealizados() {
        return totalMovimientosRealizados; 
    }
    public long getTiempoTotalJugado() {
        return tiempoTotalJugado; 
    }
    public ProgresoPorNivel getProgresoPorNivel(int nivelId) {
        return progresoPorNivel.get(nivelId); 
    }
    
    public int getNivelesCompletados() {
        int count = 0;
        for (ProgresoPorNivel progreso : progresoPorNivel.values()) {
            if (progreso.isCompletado()) count++;
        }
        return count;
    }
    
    public int getNivelesDesbloqueados() {
        int count = 0;
        for (ProgresoPorNivel progreso : progresoPorNivel.values()) {
            if (progreso.isDesbloqueado()) count++;
        }
        return count;
    }
}
