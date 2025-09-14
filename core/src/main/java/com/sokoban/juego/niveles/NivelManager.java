/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sokoban.juego.niveles;
import com.badlogic.gdx.graphics.Texture;
import com.sokoban.juego.logica.GestorUsuarios;
import com.sokoban.juego.logica.Mapas.*;
import com.sokoban.juego.logica.accounts.GestorProgreso;
import com.sokoban.juego.logica.accounts.ProgresoPorNivel;

/**
 *
 * @author hnleo
 */

public class NivelManager {
    private static NivelManager instancia;
    private GestorProgreso gestorProgreso;
    private MapaBase nivelActual;
    private int nivelActualId;
    
    private Texture muroTexture, cajaTexture, objetivoTexture, sueloTexture, jugadorTexture, cajaObjetivoTexture;
    
    private NivelManager() {
        gestorProgreso = GestorProgreso.getInstancia();
    }
    
    public static NivelManager getInstancia() {
        if (instancia == null) {
            instancia = new NivelManager();
        }
        return instancia;
    }
    
    public void inicializar(Texture muro, Texture caja, Texture objetivo, 
                           Texture suelo, Texture jugador, Texture cajaObjetivo) {
        this.muroTexture = muro;
        this.cajaTexture = caja;
        this.objetivoTexture = objetivo;
        this.sueloTexture = suelo;
        this.jugadorTexture = jugador;
        this.cajaObjetivoTexture = cajaObjetivo;
        
        // Cargar progreso del usuario actual
        if (GestorUsuarios.usuarioActual != null) {
            gestorProgreso.cargarProgreso();
        }
    }
    
    public boolean cargarNivel(int nivelId) {
        // Verificar acceso al nivel
        if (!puedeAccederANivel(nivelId)) {
            System.err.println("Acceso denegado al nivel " + nivelId);
            return false;
        }
        
        // Limpiar nivel anterior
        if (nivelActual != null) {
            nivelActual.detenerColisiones();
            nivelActual.dispose();
        }
        
        // Crear nuevo nivel según el ID
        nivelActual = crearNivel(nivelId);
        if (nivelActual != null) {
            nivelActualId = nivelId;
            nivelActual.cargarMapa();
            nivelActual.iniciarColisiones();
            
            return true;
        }
        
        return false;
    }
    
    private MapaBase crearNivel(int nivelId) {
        switch (nivelId) {
            case 1:
                return new MapaUno(10, 12, muroTexture, cajaTexture, 
                                  objetivoTexture, sueloTexture, jugadorTexture, cajaObjetivoTexture);
            case 2:
             
            // Aqui se puede agregar mas niveles samu
          
            default:
                System.err.println("Nivel no implementado: " + nivelId);
                return null;
        }
    }
    
    public boolean puedeAccederANivel(int nivelId) {
        if (GestorUsuarios.usuarioActual == null) {
            System.err.println("No hay usuario logueado");
            return false;
        }
        
        if (nivelId < 1 || nivelId > ConfigNiveles.getTotalNiveles()) {
            System.err.println("Nivel inválido: " + nivelId);
            return false;
        }
        
        return gestorProgreso.isNivelDesbloqueado(nivelId);
    }
    
    public boolean avanzarAlSiguienteNivel() {
        int siguienteNivel = nivelActualId + 1;
        
        if (siguienteNivel <= ConfigNiveles.getTotalNiveles()) {
            if (gestorProgreso.isNivelDesbloqueado(siguienteNivel)) {
                return cargarNivel(siguienteNivel);
            } else {
                System.out.println("El siguiente nivel no está desbloqueado");
                return false;
            }
        } else {
            System.out.println("¡Has completado todos los niveles disponibles!");
            mostrarResumenCompleto();
            return false;
        }
    }
    
    public void reiniciarNivelActual() {
        if (nivelActual != null) {
            cargarNivel(nivelActualId);
        }
    }
    
    private void mostrarResumenCompleto() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("¡FELICIDADES! ¡HAS COMPLETADO TODOS LOS NIVELES!");
        System.out.println("=".repeat(60));
        
        if (gestorProgreso != null) {
            gestorProgreso.mostrarEstadisticas();
        }
        
        System.out.println("Gracias por jugar nuestro juego.");
        System.out.println("=".repeat(60));
    }
    
    public void mostrarMenuNiveles() {
        System.out.println("\n=== SELECCIÓN DE NIVEL ===");
        
        for (int i = 1; i <= ConfigNiveles.getTotalNiveles(); i++) {
            String estado = "BLOQUEADO";
            String info = "";
            
            if (gestorProgreso.isNivelDesbloqueado(i)) {
                if (gestorProgreso.isNivelCompletado(i)) {
                    estado = "COMPLETADO";
                    ProgresoPorNivel progreso = gestorProgreso.getProgresoPorNivel(i);
                    if (progreso != null) {
                        info = " | " + progreso.getClasificacion() + 
                               " | Score: " + progreso.getMejorPuntaje();
                    }
                } else {
                    estado = "DISPONIBLE";
                }
            }
            
            System.out.println("Nivel " + i + ": " + ConfigNiveles.getNombreNivel(i) + 
                             " - " + estado + info);
        }
        
        System.out.println("\nNiveles completados: " + gestorProgreso.getNivelesCompletados() + 
                          "/" + ConfigNiveles.getTotalNiveles());
        System.out.println("Score total: " + gestorProgreso.getPuntajeTotalAcumulado());
        System.out.println("========================\n");
    }
    
    
    public MapaBase getNivelActual() { 
        return nivelActual;
    }
    public int getNivelActualId() { 
        return nivelActualId;
    }
    public GestorProgreso getGestorProgreso() { 
        return gestorProgreso;
    }
    
    public void dispose() {
        if (nivelActual != null) {
            nivelActual.detenerColisiones();
            nivelActual.dispose();
        }
    }
}
