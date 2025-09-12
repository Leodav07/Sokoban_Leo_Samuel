/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.logica;

/**
 *
 * @author unwir
 */
public class NivelesSokoban {
     // Array con todos los layouts de niveles
    public static final String[][] NIVELES = {
        // Nivel 1 - Inicio (Tutorial básico)
        {
            "################",
            "#@, .          ###", 
            "#                #",
            "##################",
            "##",
             ""
        },
        
        // Nivel 2 - Básico (Empujar una caja)
        {
            "#########",
            "#@      #",
            "#  ,    #",
            "#   .   #",
            "#       #",
            "#########"
        },
        
        // Nivel 3 - Esquinas (Dos cajas)
        {
            "##########",
            "#@       #",
            "#  , ,   #",
            "#        #",
            "#  . .   #",
            "#        #",
            "##########"
        },
        
        // Nivel 4 - Laberinto (Obstáculos)
        {
            "############",
            "#@    #    #",
            "#  ## # ,  #",
            "#     # .  #",
            "# ##  #    #",
            "#   , . #  #",
            "#       #  #",
            "############"
        },
        
        // Nivel 5 - Complejo (Múltiples cajas y metas)
        {
            "##############",
            "#@           #",
            "#  ,  ##  ,  #",
            "#  .     .   #",
            "#     ##     #",
            "#  ,  ##  ,  #",
            "#  .     .   #",
            "#            #",
            "##############"
        },
        
        // Nivel 6 - Desafío (Secuencia específica)
        {
            "###############",
            "#@        #   #",
            "# ,,,     #   #",
            "# ...     #   #",
            "#         #   #",
            "###   #####   #",
            "#  ,      ,   #",
            "#  .      .   #",
            "#             #",
            "###############"
        },
        
        // Nivel 7 - FINAL (Boss - Nivel complejo)
        {
            "#################",
            "#@      #       #",
            "#   ##  #  ##   #",
            "# , #      # ,  #",
            "# . #  ##  # .  #",
            "#   #  ,,  #    #",
            "#   #  ..  #    #",
            "#   #      #    #",
            "# , #  ##  # ,  #",
            "# . #      # .  #",
            "#   ##    ##    #",
            "#               #",
            "#################"
        }
    };
    
    public static final String[] NOMBRES_NIVELES = {
        "Inicio",
        "Básico", 
        "Esquinas",
        "Laberinto",
        "Complejo",
        "Desafío",
        "FINAL"
    };
    
    public static final String[] DESCRIPCIONES_NIVELES = {
        "¡Bienvenido! Empuja la caja (,) hacia la meta (.)",
        "Aprende los movimientos básicos",
        "Maneja múltiples cajas a la vez",
        "Navega entre obstáculos",
        "Planifica tu estrategia cuidadosamente",
        "Secuencia específica requerida",
        "¡El desafío final te espera!"
    };
    
    /**
     * Obtiene el layout de un nivel específico
     * @param nivel El número de nivel (1-7)
     * @return El array de strings con el layout del nivel
     */
    public static String[] getNivel(int nivel) {
        if (nivel < 1 || nivel > NIVELES.length) {
            System.err.println("Nivel inválido: " + nivel);
            return NIVELES[0]; // Retorna el primer nivel por defecto
        }
        return NIVELES[nivel - 1]; // Convertir a índice base 0
    }
    
    /**
     * Obtiene el nombre de un nivel
     */
    public static String getNombreNivel(int nivel) {
        if (nivel < 1 || nivel > NOMBRES_NIVELES.length) {
            return "Desconocido";
        }
        return NOMBRES_NIVELES[nivel - 1];
    }
    
    /**
     * Obtiene la descripción de un nivel
     */
    public static String getDescripcionNivel(int nivel) {
        if (nivel < 1 || nivel > DESCRIPCIONES_NIVELES.length) {
            return "Sin descripción";
        }
        return DESCRIPCIONES_NIVELES[nivel - 1];
    }
    
    /**
     * Método para probar un nivel específico
     */
    public static void probarNivel(int nivel) {
        System.out.println("=== NIVEL " + nivel + ": " + getNombreNivel(nivel) + " ===");
        System.out.println("Descripción: " + getDescripcionNivel(nivel));
        System.out.println();
        
        String[] layout = getNivel(nivel);
        for (String fila : layout) {
            System.out.println(fila);
        }
        System.out.println();
        
        // Contar elementos para verificar que el nivel sea válido
        int cajas = 0, metas = 0, jugadores = 0;
        for (String fila : layout) {
            for (char c : fila.toCharArray()) {
                switch (c) {
                    case ',': cajas++; break;
                    case '.': metas++; break;
                    case '*': cajas++; metas++; break; // Caja en meta
                    case '@': jugadores++; break;
                    case '!': jugadores++; metas++; break; // Jugador en meta
                }
            }
        }
        
        System.out.println("Cajas: " + cajas + ", Metas: " + metas + ", Jugadores: " + jugadores);
        
        if (cajas != metas) {
            System.out.println("¡ADVERTENCIA! Número de cajas y metas no coincide");
        }
        if (jugadores != 1) {
            System.out.println("¡ADVERTENCIA! Debe haber exactamente un jugador");
        }
        
        System.out.println("=".repeat(40));
    }
    
    // Método main para probar los niveles
    public static void main(String[] args) {
        System.out.println("PROBANDO TODOS LOS NIVELES DE SOKOBAN");
        System.out.println("====================================");
        
        for (int i = 1; i <= NIVELES.length; i++) {
            probarNivel(i);
        }
        
        System.out.println("\nPRUEBA INDIVIDUAL - NIVEL 1:");
        System.out.println("----------------------------");
        
        // Crear una instancia del GameState con el nivel 1 para probar
        String[] nivel1 = getNivel(1);
        // GameState game = new GameState(nivel1);
        // game.imprimirGame();
        
        System.out.println("\nNivel 1 cargado correctamente:");
        for (String fila : nivel1) {
            System.out.println(fila);
        }
    }
}

