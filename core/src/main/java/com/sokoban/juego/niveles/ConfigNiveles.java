/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.niveles;

/**
 *
 * @author hnleo
 */
public class ConfigNiveles {
       
    public static class ConfigNivel {
        public final int movimientosObjetivo;
        public final long tiempoObjetivo;
        public final String nombre;
        public final String descripcion;
        
        public ConfigNivel(String nombre, String descripcion, int movimientosObjetivo, long tiempoObjetivoSegundos) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.movimientosObjetivo = movimientosObjetivo;
            this.tiempoObjetivo = tiempoObjetivoSegundos * 1000; // Convertir a milisegundos
        }
    }
    
    private static final ConfigNivel[] CONFIGURACIONES = {
        null,
        new ConfigNivel("Introducción", "Aprende los conceptos básicos", 40, 120),
        new ConfigNivel("Primeros Pasos", "Manejo básico de cajas", 60, 150),
        new ConfigNivel("Obstáculos", "Navega alrededor de obstáculos", 85, 180),
        new ConfigNivel("Estrategia", "Planifica tus movimientos", 110, 210),
        new ConfigNivel("Precisión", "Cada movimiento cuenta", 90, 240),
        new ConfigNivel("Laberinto", "Encuentra el camino correcto", 150, 300),
        new ConfigNivel("Maestría", "El desafío final", 180, 360)
    };
    
    public static ConfigNivel getConfig(int nivelId) {
        if (nivelId < 1 || nivelId >= CONFIGURACIONES.length) {
            throw new IllegalArgumentException("Nivel inválido: " + nivelId);
        }
        return CONFIGURACIONES[nivelId];
    }
    
    public static int getMovimientosObjetivo(int nivelId) {
        return getConfig(nivelId).movimientosObjetivo;
    }
    
    public static long getTiempoObjetivo(int nivelId) {
        return getConfig(nivelId).tiempoObjetivo;
    }
    
    public static String getNombreNivel(int nivelId) {
        return getConfig(nivelId).nombre;
    }
    
    public static String getDescripcionNivel(int nivelId) {
        return getConfig(nivelId).descripcion;
    }
    
    public static int getTotalNiveles() {
        return CONFIGURACIONES.length - 1;
    }
}
