///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.sokoban.juego.logica;
//
//
//import com.sokoban.juego.logica.accounts.GestorProgreso;
//import java.util.Scanner;
///**
// *
// * @author unwir
// */
//public class GameScreenNiveles {
//    
//    private GameState gameState;
//    private int nivelActual;
//    private Scanner scanner;
//    private GestorProgreso gestorProgreso;
//    
//    public GameScreenNiveles(int nivelId) {
//        this.nivelActual = nivelId;
//        this.scanner = new Scanner(System.in);
//        this.gestorProgreso = GestorProgreso.getInstancia();
//        
//        String[] layoutNivel = NivelesSokoban.getNivel(nivelId);
//        this.gameState = new GameState(layoutNivel, nivelId);
//        
//        System.out.println("=== " + NivelesSokoban.getNombreNivel(nivelId) + " ===");
//        System.out.println(NivelesSokoban.getDescripcionNivel(nivelId));
//        System.out.println();
//    }
//    
//    public void jugar() {
//        String input;
//        
//        while (!gameState.verificarVictoria()) {
//            gameState.imprimirGame();
//            System.out.print("Movimiento (wasd) o 'q' para salir: ");
//            input = scanner.next();
//            
//            if (input.equalsIgnoreCase("q")) {
//                System.out.println("Saliendo del nivel...");
//                return;
//            }
//            
//            switch (input.toLowerCase()) {
//                case "w": gameState.moverPlayer(0, -1); break;
//                case "s": gameState.moverPlayer(0, 1); break;
//                case "a": gameState.moverPlayer(-1, 0); break;
//                case "d": gameState.moverPlayer(1, 0); break;
//                default: 
//                    System.out.println("Tecla inválida. Usa WASD para moverte.");
//                    break;
//            }
//        }
//        
//        if (gameState.verificarVictoria()) {
//            System.out.println("\n🎉 ¡NIVEL COMPLETADO! 🎉");
//            gameState.imprimirGame();
//            
//            gestorProgreso.completarNivel(nivelActual, gameState.getContadorMovimientos(), gameState.getTiempoTranscurrido());
//            
//            if (nivelActual == 7) {
//                System.out.println("🏆 ¡FELICIDADES! ¡HAS COMPLETADO TODOS LOS NIVELES! 🏆");
//            } else {
//                System.out.println("¡Nivel " + (nivelActual + 1) + " desbloqueado!");
//            }
//            
//            System.out.println("Presiona Enter para continuar...");
//            scanner.nextLine(); // Consumir el \n pendiente
//            scanner.nextLine(); // Esperar Enter del usuario
//        }
//    }
//    
//    public static void main(String[] args) {
//        System.out.println("¿Qué nivel quieres probar? (1-7): ");
//        Scanner sc = new Scanner(System.in);
//        int nivel = sc.nextInt();
//        
//        if (nivel >= 1 && nivel <= 7) {
//            GameScreenNiveles game = new GameScreenNiveles(nivel);
//            game.jugar();
//        } else {
//            System.out.println("Nivel inválido. Debe ser entre 1 y 7.");
//        }
//        
//        sc.close();
//    }
//    
//}
